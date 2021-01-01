@ExperimentalUnsignedTypes
data class Segment(val name: String, var position: UShort?, var length: Int, val tokens: List<String>) {
    override fun toString(): String {
        return "Segment(name='$name', position=$position, length=$length)"
    }
}

@ExperimentalUnsignedTypes
data class Marker(val name: String, val position: UShort)

@ExperimentalUnsignedTypes
data class SegmentTranslationRslt(val valid: Boolean, val binaryCode: List<UByte>, val markersRelative: Map<String, Int>)

@ExperimentalUnsignedTypes
private fun translate(segment: Segment, segments: Map<String, Segment>, markers: Map<String, Marker>): SegmentTranslationRslt {
    var valid = true
    val target = ArrayList<UByte>()
    val relativeMarkers = mutableMapOf<String, Int>()

    val tokenStream = segment.tokens.iterator()
    while (tokenStream.hasNext()) {
        val token = tokenStream.next()

        // Preload D macro
        when  {
            token == ".goto" -> {
                val target_name = tokenStream.next()
                val target_segment = segments[target_name]
                val target_marker = markers[target_name]

                if (target_segment == null && target_marker == null) {
                    valid = false
                }

                val jmp_predicted_location = (segment.position?.toInt() ?: 0) + target.size + 9 + 1

                val target_address_absolute = target_segment?.position?.toInt() ?: target_marker?.position?.toInt() ?: 0

                val target_address_real = ((target_address_absolute/* - jmp_predicted_location*/) and 0xFFFF).toUShort()
                println("target $target_name: $target_address_real bytes ahead")
                val target_address_lower = (target_address_real and 0x00FFu).toUByte()
                val target_address_upper = (target_address_real and 0xFF00u).toInt().shr(8).toUByte()

                target.add(Instructions.PSH.opcode.toUByte())
                target.add(Instructions.LDC.opcode.toUByte())
                target.add(target_address_lower)
                target.add(Instructions.CPL.opcode.toUByte())
                target.add(Instructions.LDC.opcode.toUByte())
                target.add(target_address_upper)
                target.add(Instructions.CPH.opcode.toUByte())
                target.add(Instructions.SEE.opcode.toUByte())
                target.add(Instructions.POP.opcode.toUByte()) // +9

                target.add(Instructions.JMP.opcode.toUByte()) // +1
            }
            token == ".inline" -> {
                val target_segment_name = tokenStream.next()
                val target_segment = segments[target_segment_name]

                if (target_segment == null) {
                    throw Exception("Can only inline segments declared earlier!")
                }

                val translated = translate(target_segment, segments, markers)
                target.addAll(translated.binaryCode)

                if(!translated.valid) {
                    valid = false
                }
            }
            token == ".die" -> {
                // Outputting -1 kills the interpreter
                target.add(Instructions.LDC.opcode.toUByte())
                target.add(0xFFu)
                target.add(Instructions.OUT.opcode.toUByte())
            }
            token.startsWith("@") -> {
                val markerName = token.removePrefix("@")
                relativeMarkers[markerName] = target.size
            }
            else -> {
                val instruction =
                    Instructions.values().find { it.mnemonic == token } ?: throw Exception("Unknown instruction: $token")
                target.add(instruction.opcode.toUByte())
                if (instruction.immediateDataBytes == 1) {
                    val dataToken = tokenStream.next()
                    val data = if (dataToken.startsWith("0x")) {
                        java.lang.Long.parseLong(dataToken.removePrefix("0x"), 16)
                    } else {
                        java.lang.Long.parseLong(dataToken)
                    }

                    if (data !in 0..255) {
                        throw Exception("Out of range literal: $data")
                    }

                    val dataByte: UByte = data.toUByte()
                    target.add(dataByte)
                }
            }
        }
    }

    return SegmentTranslationRslt(valid, target, relativeMarkers)
}

@ExperimentalUnsignedTypes
fun assemble(string: String): List<UByte> {
    val target = ArrayList<UByte>()
    val tokens = string.lines().map { val indexOf = it.indexOf("//"); if (indexOf == -1) it else it.substring(0, indexOf) }
        .joinToString(separator = " ").replace("\t", " ").split(" ").filter { it != "" }

    val markers = mutableMapOf<String, Marker>()
    var segments = mutableMapOf<String, Segment>()
    var currentSegmentName: String? = null
    var currentSegmentTokens = mutableListOf<String>()

    fun finishSegment() {
        if(currentSegmentName != null) {
            val segment = Segment(currentSegmentName!!, null, 0, currentSegmentTokens)
            segment.length = translate(segment, segments, emptyMap()).binaryCode.size

            segments[currentSegmentName!!] = segment
            currentSegmentTokens = mutableListOf()
        }
    }

    val tokenStream = tokens.iterator()
    while (tokenStream.hasNext()) {
        val token = tokenStream.next()

        if (token == ".segment") {
            val segmentName = tokenStream.next()
            finishSegment()
            currentSegmentName = segmentName
        } else {
            currentSegmentTokens.add(token)
        }
    }
    finishSegment()

    if(segments["main"] == null) {
        throw Exception("Lacking a main segment")
    }

    // Place segments somewhere
    val segmentsInOrder = mutableListOf<Segment>()
    var nextFree: UShort = 0x8000u
    val mainSegment = segments["main"]!!
    segmentsInOrder.add(mainSegment)
    mainSegment.position = nextFree
    nextFree = (nextFree + mainSegment.length.toUInt()).toUShort()

    while(true) {
        val unplaced_segment = segments.values.find { it.position == null } ?: break
        segmentsInOrder.add(unplaced_segment)
        unplaced_segment.position = nextFree
        nextFree = (nextFree + unplaced_segment.length.toUInt()).toUShort()
    }

    // Resolve markers
    for(segment in segmentsInOrder) {
        val translated = translate(segment, segments, markers)
        for(relMarker in translated.markersRelative) {
            markers[relMarker.key] = Marker(relMarker.key, (segment.position!!.toUInt() + relMarker.value.toUInt()).toUShort())
        }
    }

    println("Segments:")
    println(segmentsInOrder.joinToString(separator = "\n"))

    println("Markers:")
    println(markers.values.joinToString(separator = "\n"))

    for(segment in segmentsInOrder) {
        val translated = translate(segment, segments, markers)
        if(!translated.valid) {
            throw Exception("fatal: something went wrong when allocating segments")
        }

        target.addAll(translated.binaryCode)
    }

    return target
}
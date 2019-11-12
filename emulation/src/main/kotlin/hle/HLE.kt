package hle

import Instructions

@kotlin.ExperimentalUnsignedTypes
class Xol16ComputerHLE(val rom: UByteArray) {
    val ram = UByteArray(256)

    var pc: UShort = 0x8000u

    var a: UByte = 0u
    var b: UByte = 0u
    var c: Boolean = false
    var d: UShort = 0u

    var stack: MutableList<UByte> = mutableListOf()

    var stop = false

    fun clock() {
        val instructionOpcode = memRead(pc)
        //println("exec: $instructionOpcode $this")
        pc = pc.inc()

        when (Instructions.values()[instructionOpcode.toInt()]) {
            Instructions.ADD -> {
                val rslt = a + b
                a = (rslt).toUByte()
                c = rslt > 255u
            }
            Instructions.NOR -> {
                a = (a.or(b)).inv()
            }
            Instructions.CPB -> {
                b = a
            }
            Instructions.CMP -> {
                val equal = a == b
                c = equal
                a = if (equal) 1u else 0u
            }
            Instructions.CMS -> {
                val inf = a < b
                c = inf
                a = if (inf) 1u else 0u
            }
            Instructions.JMP -> {
                pc = (pc + d).toUShort()
            }
            Instructions.JMF -> {
                if (c) {
                    pc = (pc + d).toUShort()
                }
            }
            Instructions.CPL -> {
                d = (d and 0xFF00u) or a.toUShort()
            }
            Instructions.CPH -> {
                d = (a.toInt().shl(8).toUShort()) or (d and 0x00FFu)
            }
            Instructions.LDL -> {
                val extraData = memRead(pc)
                pc = pc.inc()
                d = (d and 0xFF00u) or extraData.toUShort()
            }
            Instructions.LDH -> {
                val extraData = memRead(pc)
                pc = pc.inc()
                d = (extraData.toInt().shl(8).toUShort()) or (d and 0x00FFu)
            }
            Instructions.LDC -> {
                val extraData = memRead(pc)
                pc = pc.inc()
                a = extraData
            }
            Instructions.GET -> {
                a = memRead(d)
            }
            Instructions.PUT -> {
                memWrite(d, a)
            }
            Instructions.PSH -> {
                stack.add(a)
            }
            Instructions.PEK -> {
                a = stack.last()
            }
            Instructions.POP -> {
                a = stack.removeAt(stack.size - 1)
            }
            Instructions.OUT -> {
                if (a == 0xFFu.toUByte()) {
                    stop = true
                } else {
                    println("OUTPUT: $a (${a.toByte().toChar()})")
                }
            }
            else -> throw Exception("Illegal instruction")
        }
    }

    fun memRead(address: UShort): UByte = when (address) {
        in 0u..0xFFu -> ram[address.toInt()]
        in 0x8000u..0xFFFFu -> rom[(address and 0x7FFFu).toInt() % rom.size]
        else -> 0u
    }

    fun memWrite(address: UShort, data: UByte) = when (address) {
        in 0u..0xFFu -> ram[address.toInt()] = data
        else -> Unit
    }

    override fun toString(): String {
        return "Xol16ComputerHLE(pc=$pc, a=$a, b=$b, c=$c, d=$d stack_size:${stack.size})"
    }
}
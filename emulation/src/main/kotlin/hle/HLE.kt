package hle

import Instructions
import java.lang.Exception

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

        val instruction = Instructions.values().find { it.opcode.toUByte() == instructionOpcode } ?: throw Exception("Invalid opcode $instructionOpcode")
        when (instruction) {
            Instructions.ADD -> {
                val result = a + b
                a = (result).toUByte()
                c = result > 255u
            }
            Instructions.NOR -> {
                a = (a.or(b)).inv()
            }
            Instructions.CMP -> {
                val inf = a < b
                c = inf
                a = if (inf) 1u else 0u
            }
            Instructions.EQU -> {
                val equal = a == b
                c = equal
                a = if (equal) 1u else 0u
            }

            Instructions.JMP -> {
                pc = (pc + d).toUShort()
            }
            Instructions.JMF -> {
                if (c) {
                    pc = (pc + d).toUShort()
                }
            }

            Instructions.CPB -> {
                b = a
            }
            Instructions.CPL -> {
                d = (d and 0xFF00u) or a.toUShort()
            }
            Instructions.CPH -> {
                d = (a.toInt().shl(8).toUShort()) or (d and 0x00FFu)
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
            Instructions.SEE -> {
                a = stack.last()
            }
            Instructions.POP -> {
                stack.removeAt(stack.size - 1)
            }
            Instructions.INP -> {
                val line = readLine() ?: "0"
                val nbr = line.toIntOrNull() ?: 0
                a = (nbr and 0xFF).toUByte()
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
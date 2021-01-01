package hle

import CPU
import Instructions
import Machine
import java.lang.Exception

@kotlin.ExperimentalUnsignedTypes
class HLE_CPU(machine: Machine) : CPU(machine) {
    var pc: UShort = 0x8000u

    var a: UByte = 0u
    var b: UByte = 0u
    var c: Boolean = false
    var d: UShort = 0u

    var stack: MutableList<UByte> = mutableListOf()

    override fun clock() {
        val instructionOpcode = machine.memRead(pc)
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
            Instructions.IFC -> {
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
                val extraData = machine.memRead(pc)
                pc = pc.inc()
                a = extraData
            }
            Instructions.GET -> {
                a = machine.memRead(d)
            }
            Instructions.PUT -> {
                machine.memWrite(d, a)
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
                a = machine.input()
            }
            Instructions.OUT -> {
                machine.output(a)
            }
            else -> throw Exception("Illegal instruction")
        }
    }

    override fun toString(): String {
        return "HLE_CPU(pc=$pc, a=$a, b=$b, c=$c, d=$d stack_size:${stack.size})"
    }
}
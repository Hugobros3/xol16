package lle

import CPU
import Machine

enum class ALU_OP { ADD, NOR, INF, EQU, UNDEF }
enum class ADDR_GEN { `PC+1`, `PC+2`, UNDEF }
enum class MEM_ADDR { A_GEN, D, PICK_USING_C, UNDEF }
enum class A_SRC { ALU_O, MEM_O, SR_O, STDIN, UNDEF }
enum class MEM_OP { READ, WRITE, UNDEF }
enum class SR_DIR { LEFT, RIGHT, UNDEF }

@ExperimentalUnsignedTypes
class LLE_CPU(machine: Machine) : CPU(machine) {
    data class Control(
        // ALU related stuff
        val alu_op: ALU_OP = ALU_OP.ADD, val w_c: Boolean,
        val w_b: Boolean,
        val addr_gen: ADDR_GEN = ADDR_GEN.UNDEF,
        // Program counter stuff
        val w_pc: Boolean,
        val w_a: Boolean, val a_src: A_SRC = A_SRC.UNDEF,
        val mem_addr: MEM_ADDR = MEM_ADDR.UNDEF, val mem_op: MEM_OP = MEM_OP.UNDEF,
        val w_inst: Boolean, val w_d_lo: Boolean, val w_d_hi: Boolean,
        // Shift register stuff
        val sr_pulse: Boolean, val sr_dir: SR_DIR = SR_DIR.UNDEF,
        val pulse_out: Boolean,
    )

    val control_step_1 = mapOf(
        Instructions.ADD to Control(alu_op = ALU_OP.ADD, w_c =  true, w_b = false, w_pc = false, w_a =  true, a_src = A_SRC.ALU_O, w_inst = false, w_d_lo = false, w_d_hi = false, sr_pulse = false, pulse_out = false),
        Instructions.NOR to Control(alu_op = ALU_OP.NOR, w_c = false, w_b = false, w_pc = false, w_a =  true, a_src = A_SRC.ALU_O, w_inst = false, w_d_lo = false, w_d_hi = false, sr_pulse = false, pulse_out = false),
        Instructions.CMP to Control(alu_op = ALU_OP.INF, w_c =  true, w_b = false, w_pc = false, w_a = false,                      w_inst = false, w_d_lo = false, w_d_hi = false, sr_pulse = false, pulse_out = false),
        Instructions.EQU to Control(alu_op = ALU_OP.EQU, w_c =  true, w_b = false, w_pc = false, w_a = false,                      w_inst = false, w_d_lo = false, w_d_hi = false, sr_pulse = false, pulse_out = false),

        Instructions.JMP to Control(w_c = false, w_b = false, w_pc = false, w_a = false, w_inst = false, w_d_lo = false, w_d_hi = false, sr_pulse = false, pulse_out = false),
        Instructions.IFC to Control(w_c = false, w_b = false, w_pc = false, w_a = false, w_inst = false, w_d_lo = false, w_d_hi = false, sr_pulse = false, pulse_out = false),

        Instructions.CPB to Control(w_c = false, w_b =  true, w_pc = false, w_a = false, w_inst = false, w_d_lo = false, w_d_hi = false, sr_pulse = false, pulse_out = false),
        Instructions.CPL to Control(w_c = false, w_b = false, w_pc = false, w_a = false, w_inst = false, w_d_lo =  true, w_d_hi = false, sr_pulse = false, pulse_out = false),
        Instructions.CPH to Control(w_c = false, w_b = false, w_pc = false, w_a = false, w_inst = false, w_d_lo = false, w_d_hi =  true, sr_pulse = false, pulse_out = false),

        Instructions.LDC to Control(w_c = false, w_b = false, addr_gen = ADDR_GEN.`PC+1`, w_pc = false, w_a = true, a_src = A_SRC.MEM_O, mem_addr = MEM_ADDR.A_GEN, mem_op =  MEM_OP.READ, w_inst = false, w_d_lo = false, w_d_hi = false, sr_pulse = false, pulse_out = false),
        Instructions.GET to Control(w_c = false, w_b = false,                             w_pc = false, w_a = true, a_src = A_SRC.MEM_O, mem_addr = MEM_ADDR.D    , mem_op =  MEM_OP.READ, w_inst = false, w_d_lo = false, w_d_hi = false, sr_pulse = false, pulse_out = false),
        Instructions.PUT to Control(w_c = false, w_b = false,                             w_pc = false, w_a = false,                     mem_addr = MEM_ADDR.D    , mem_op = MEM_OP.WRITE, w_inst = false, w_d_lo = false, w_d_hi = false, sr_pulse = false, pulse_out = false),

        Instructions.PSH to Control(w_c = false, w_b = false, w_pc = false, w_a = false,                    w_inst = false, w_d_lo = false, w_d_hi = false, sr_pulse =  true, sr_dir = SR_DIR.RIGHT, pulse_out = false),
        Instructions.SEE to Control(w_c = false, w_b = false, w_pc = false, w_a = true, a_src = A_SRC.SR_O, w_inst = false, w_d_lo = false, w_d_hi = false, sr_pulse = false,                        pulse_out = false),
        Instructions.POP to Control(w_c = false, w_b = false, w_pc = false, w_a = false,                    w_inst = false, w_d_lo = false, w_d_hi = false, sr_pulse =  true, sr_dir = SR_DIR.LEFT, pulse_out = false),

        // TODO
        Instructions.INP to Control(w_c = false, w_b = false, w_pc = false, w_a = false, w_inst = false, w_d_lo = false, w_d_hi = false, sr_pulse = false, pulse_out = false),
        Instructions.OUT to Control(w_c = false, w_b = false, w_pc = false, w_a = false, w_inst = false, w_d_lo = false, w_d_hi = false, sr_pulse = false, pulse_out = true),
    )

    val control_step_2 = Instructions.values().map { instruction ->
        val mem_addr = when(instruction) {
                Instructions.JMP -> MEM_ADDR.D
                Instructions.IFC -> MEM_ADDR.PICK_USING_C
                else -> MEM_ADDR.A_GEN
            }
        val addr_gen = when(instruction.immediateDataBytes) {
            0 -> ADDR_GEN.`PC+1`
            1 -> ADDR_GEN.`PC+2`
            else -> throw Exception("No address generation modes account for this")
        }
        Pair(instruction, Control(w_c = false, w_b = false, addr_gen = addr_gen, w_pc = true, /*pc_src = pc_src,*/ w_a = false, mem_addr = mem_addr, mem_op = MEM_OP.READ, w_inst = true, w_d_lo = false, w_d_hi = false, sr_pulse = false, pulse_out = false))
    }.toMap()

    var control_rom = listOf(control_step_1, control_step_2)

    var program_counter: UShort = 0x7FFFu
    var current_instruction: UByte = 0x0u
    var a: UByte = 0u
    var b: UByte = 0u
    var c: Boolean = false
    var d: UShort = 0u
    var shift_register = Array<UByte>(32) { 0u }

    var cycle_state = 1

    override fun clock() {
        val latched_a = a
        val decoded_instruction = Instructions.values().find { it.opcode.toUByte() == current_instruction }!!
        val control_lines: Control = control_rom[cycle_state][decoded_instruction]!!

        val alu_o_pair: Pair<UByte?, Boolean?>? = when (control_lines.alu_op) {
            ALU_OP.ADD -> {
                val result = a + b
                Pair((result).toUByte(), result > 255u)
            }
            ALU_OP.NOR -> {
                val result = (a.or(b)).inv()
                Pair(result, null)
            }
            ALU_OP.INF -> {
                val inf = a < b
                Pair(null, inf)
            }
            ALU_OP.EQU -> {
                val equal = a == b
                Pair(null, equal)
            }
            ALU_OP.UNDEF -> null
        }
        val alu_a_o = alu_o_pair?.first
        val alu_c_o = alu_o_pair?.second

        val mem_address = run {
            val address_generator_o = when (control_lines.addr_gen) {
                ADDR_GEN.`PC+1` -> program_counter + 1u
                ADDR_GEN.`PC+2` -> program_counter + 2u
                ADDR_GEN.UNDEF -> null
            }?.toUShort()

            when (control_lines.mem_addr) {
                MEM_ADDR.A_GEN -> address_generator_o
                MEM_ADDR.D -> d
                MEM_ADDR.PICK_USING_C -> if (c) d else address_generator_o
                MEM_ADDR.UNDEF -> null
            }
        }

        var mem_rslt: UByte? = null
        when (control_lines.mem_op) {
            MEM_OP.READ -> {
                mem_rslt = machine.memRead(mem_address!!)
            }
            MEM_OP.WRITE -> {
                machine.memWrite(mem_address!!, latched_a)
            }
            MEM_OP.UNDEF -> {
            }
        }

        if (control_lines.sr_pulse) {
            when (control_lines.sr_dir) {
                SR_DIR.LEFT -> {
                    for (i in 1 until shift_register.size) {
                        shift_register[i - 1] = shift_register[i]
                    }
                    shift_register[shift_register.size - 1] = 0u
                }
                SR_DIR.RIGHT -> {
                    for (i in 0 until shift_register.size - 1) {
                        shift_register[i + 1] = shift_register[i]
                    }
                    shift_register[0] = latched_a
                }
                SR_DIR.UNDEF -> throw Exception("Pulsing shift register without asserting SR_DIR")
            }
        }

        if (control_lines.pulse_out) {
            machine.output(latched_a)
        }

        if (control_lines.w_a) {
            a = when(control_lines.a_src) {
                A_SRC.ALU_O -> alu_a_o!!
                A_SRC.MEM_O -> mem_rslt!!
                A_SRC.SR_O -> shift_register[0]
                A_SRC.STDIN -> machine.input()
                A_SRC.UNDEF -> throw Exception("Asserting write a without defining a_src")
            }
        }

        if (control_lines.w_a && control_lines.w_b)
            throw Exception("Incompatible flags")

        if (control_lines.w_b) {
            b = latched_a
        }

        if (control_lines.w_c) {
            c = alu_c_o!!
        }

        if (control_lines.w_pc) {
            program_counter = mem_address!!
        }

        if (control_lines.w_inst) {
            current_instruction = mem_rslt!!
        }

        if (control_lines.w_d_lo) {
            d = (d and 0xFF00u) or a.toUShort()
        }

        if (control_lines.w_d_hi) {
            d = (a.toInt().shl(8).toUShort()) or (d and 0x00FFu)
        }

        cycle_state = 1 - cycle_state
    }

    override fun toString(): String {
        return "LLE_CPU(program_counter=$program_counter, current_instruction=$current_instruction, a=$a, b=$b, c=$c, d=$d, shift_register=${shift_register.contentToString()}, cycle_state=$cycle_state)"
    }
}
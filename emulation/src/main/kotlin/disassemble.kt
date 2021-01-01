@ExperimentalUnsignedTypes
fun disassemble(stuff: List<UByte>) {
    var i = 0
    while (i < stuff.size) {
        val opcode = stuff[i]
        val instr = Instructions.values().find { it.opcode.toUByte() == opcode }!!
        val payload = if (instr.immediateDataBytes > 0) {
            i++
            stuff[i]
        } else null
        if (payload != null)
            println("${i - 1} ${instr.mnemonic} $payload")
        else
            println("$i ${instr.mnemonic}")
        i++
    }
}
@UseExperimental(ExperimentalUnsignedTypes::class)
enum class Instructions(val hasImmediateData: Int) {
    ADD(0),
    NOR(0),
    CPB(0),
    CMP(0),
    CMS(0),
    JMP(0),
    JMF(0),
    CPL(0),
    CPH(0),
    LDL(1),
    LDH(1),
    LDC(1),
    GET(0),
    PUT(0),
    PSH(0),
    PEK(0),
    POP(0),
    OUT(0),
    ;

    val mnemonic = this.name.toLowerCase()
    val opcode = this.ordinal.toUByte()
}
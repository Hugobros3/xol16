@UseExperimental(ExperimentalUnsignedTypes::class)
enum class Instructions(val opcode: Int, val immediateDataBytes: Int) {
    ADD(0x0, 0),
    NOR(0x1, 0),
    CMP(0x2, 0),
    EQU(0x3, 0),

    JMP(0x4, 0),
    JMF(0x5, 0),

    CPB(0x8, 0),
    CPL(0xA, 0),
    CPH(0xB, 0),

    LDC(0xC, 1),
    GET(0xD, 0),
    PUT(0xE, 0),

    PSH(0x10, 0),
    SEE(0x11, 0),
    POP(0x12, 0),

    INP(0x1E, 0),
    OUT(0x1F, 0),
    ;

    val mnemonic = this.name.toLowerCase()
}
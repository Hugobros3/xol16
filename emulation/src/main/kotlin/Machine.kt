
@ExperimentalUnsignedTypes
abstract class CPU(val machine: Machine) {
    abstract fun clock()
}

@ExperimentalUnsignedTypes
open class Machine(val rom: UByteArray) {
    val ram = UByteArray(256)

    open fun memRead(address: UShort): UByte = when (address) {
        in 0u..0xFFu -> ram[address.toInt()]
        in 0x8000u..0xFFFFu -> rom[(address and 0x7FFFu).toInt() % rom.size]
        else -> 0u
    }

    open fun memWrite(address: UShort, data: UByte) = when (address) {
        in 0u..0xFFu -> ram[address.toInt()] = data
        else -> Unit
    }

    open fun output(byte: UByte) {
        println("OUTPUT: $byte (${byte.toByte().toChar()})")
    }

    open fun input(): UByte {
        val line = readLine() ?: "0"
        val nbr = line.toIntOrNull() ?: 0
        return (nbr and 0xFF).toUByte()
    }
}

@ExperimentalUnsignedTypes
open class StoppableMachine(rom: UByteArray) : Machine(rom) {
    var stop = false

    override fun output(byte: UByte) {
        if (byte == 0xFF.toUByte())
            stop = true
        super.output(byte)
    }
}
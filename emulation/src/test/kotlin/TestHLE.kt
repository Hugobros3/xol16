import hle.HLE_CPU
import lle.LLE_CPU
import org.junit.Test
import java.io.File

@ExperimentalUnsignedTypes
class TestMachine(rom: UByteArray, val givenInput: List<UByte>, val expectedOutput: List<UByte>) : Machine(rom) {
    fun go(cpu: CPU) {
        while (true) {
            if (oup >= expectedOutput.size)
                break
            cpu.clock()
        }
    }

    var inp = 0
    override fun input(): UByte {
        if (inp >= givenInput.size)
            throw Exception("Read too much")
        return givenInput[inp++]
    }

    var oup = 0
    override fun output(byte: UByte) {
        assert(byte == expectedOutput[oup++])
        super.output(byte)
    }
}

@ExperimentalUnsignedTypes
class TestHLE {
    @Test
    fun testHelloWorld() {
        val str = File("examples/hello_world.x16").readText()
        val assembled = assemble(str).toUByteArray()
        val hw = "HELLO WORLD\u00ff".toCharArray().toList().map { it.toInt().toUByte() }
        val machine = TestMachine(assembled, emptyList(), hw)
        val cpu = LLE_CPU(machine)
        machine.go(cpu)
    }

    @Test
    fun testSegments() {
        val str = File("examples/segments_test.x16").readText()
        val assembled = assemble(str).toUByteArray()
        disassemble(assembled.toList())
        val machine = TestMachine(assembled, emptyList(), listOf<UByte>(0u, 1u, 2u, 57u))
        val cpu = LLE_CPU(machine)
        machine.go(cpu)
    }
}
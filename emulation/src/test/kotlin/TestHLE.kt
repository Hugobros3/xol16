import hle.Xol16ComputerHLE
import org.junit.Test
import java.io.File

class TestHLE {
    @Test
    fun testHelloWorld() {
        val str = File("examples/hello_world.x16").readText()
        val assembled = assemble(str).toUByteArray()
        val machine = Xol16ComputerHLE(assembled)

        while(!machine.stop) {
            machine.clock()
        }
    }

    @Test
    fun testSegments() {
        val str = File("examples/segments_test.x16").readText()
        val assembled = assemble(str).toUByteArray()
        val machine = Xol16ComputerHLE(assembled)

        while(!machine.stop) {
            machine.clock()
        }
    }
}
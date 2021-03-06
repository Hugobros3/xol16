import hle.HLE_CPU
import java.io.File

@ExperimentalUnsignedTypes
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Xol16 8-bit Minecraft computer emulator")
        println("For design evaluation and validation purposes")
        println("COMMANDS:")
        println("assemble [filename] - assembler")
        return
    }

    when {
        args[0] == "assemble" -> {
            val file = File(args[1])
            val assembled = assemble(file.readText())
            println(assembled)
        }
        args[0] == "run-hle" -> {
            val file = File(args[1])
            val assembled = assemble(file.readText()).toUByteArray()
            val machine = StoppableMachine(assembled)

            val cpu = HLE_CPU(machine)

            while(!machine.stop) {
                cpu.clock()
            }
        }
    }
}
# XOL-16 Redstone computer

A computer simple enough to build in Minecraft (hopefully) !

## Project

 * Simple enough that I can finish it without losing interest
 * Can run interesting stuff
 * Write a simple assembler/mayyybe retarget a C compiler ?
 * Flex on the freshman kids who struggle with their CPU class :D

## Features

* 16-bit address bus
* 8-bit data bus
* Add and Nor. No sub, no other bitwise ops.
* Absolute branching
* One(!) addressing mode via a dedicated register (+ immediate loads)

# Instruction Set ( wip )

Registers:

* A: **A**ccumulator, 8b
* B: O🅱️erand, 8b
* C: **C**ondition, boolean
* D: **D**elta, 16b: For branching

| mnemonic | effects | commentary |
| --- | --- | --- |
| add | a = a + b, c = overflow | vanilla normal addition |
| nor | a = !(a \| b) | complete singleton logic operand |
| cpb | b = a | Copies a into b |
| cmp | c = a == b | compare a and b |
| cms | c = a < b | *strict* comparison |
| jmp | pc = d | Inconditional jump |
| jmc | if(c == true) pc = d | Conditional jump |
| cpl | d[7..0] = a | Copy A to lower 8 bits of delta register |
| cph | d[15..8] = a | Copy A to upper 8 bits of delta register |
| ldl | d[7..0] = \*(pc++) | Loads constant into lower 8 bits of delta register |
| ldh | d[15..8] = \*(pc++) | Loads constant into upper 8 bits of delta register |
| ldc | a = \*(pc++) | Load from constant |
| get | a = *d | Load from address |
| put | *d = a | Store to address |
| psh | push(a) | Push A contents to stack |
| pek | a = peek() | Read the contents of the top of the stack and puts it into A |
| pop | a = pop() | Pop stack and reads it's contents in A |
| out | print(a) | Sends the content of A to the standard output |

<!--
| drf | d = *d | D is dereferenced and updates it's lower 8 bits |
| off | d += *(pc++) as u16 | Offset D by a constant, sign-extended to 16b | -->

# rationale

## nor + cpb are all the logic you'd need

proof:

### not

```
cpb // b = a
nor // a = !(a | b) = !(a | a) = !a
```

### or
```
nor // a = !(a | b)
#not
```

### and
```
#not b ( not it before loading it into b)
#not a
nor // a = !(!a | !b) = !!a & !!b = a & b
```

## implementing sub

```
ldc // load rhs
cpb
nor // not'ed rhs
cpb
ldc 0x01
add // done inversing rhs
cpb
ldc // load lhs
add
```

# todo

 * is a inversed version of jmp ( if a != 0 ) useful ?
 * is cms/cmp redudant given we can == with xor ?

# implem

* a_out/16
* d_out/8
* d_in/8
* read/write line
* request line
* reply line
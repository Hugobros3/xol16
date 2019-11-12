# Control logic mental dump

TODO: update wrt revised instructions

1) ALU: PC + 0, mem_read, latch data into INREG
2) (optional) ALU: PC + 1, no_mem, latch address into PC
3) ALU: instruction-dependant setup, maybe fetch
4) ALU: PC + 1, no_mem, latch address into PC

Control lines per instruction:
# add:
ALU_left: A
ALU_right: B
A_latch: true
A_mux: ALU_low
ALU_op: add
C_latch: true
memory: nop

# nor: 
ALU_left: A
ALU_right: B
A_latch: true
A_mux: ALU_low
ALU_op: nor
memory: nop

# cpb
B_latch: true

# cmp
ALU_left: A
ALU_right: B
C_latch: true
ALU_op: eq
memory: nop

# cms: 
ALU_left: A
ALU_right: !B
C_latch: true
ALU_op: add
memory: nop

#jmp
PC_latch: C
ALU_left: D
ALU_right: PC

#jmc
PC_latch: !C
ALU_left: D
ALU_right: PC

#cpl
D_latch_lower: true
D_lower_mux: A

#cph
D_latch_upper: true
D_upper_mux: A

#ldc
load_extra: true
addr_mux: PC
memory: read
A_latch: true
A_mux: bus_data_in

#get
addr_mux: D
memory: read
A_latch: true
A_mux: bus_data_in

#put
addr_mux: D
memory: write

#ldl
load_extra: true
D_latch_lower: true
D_lower_mux: bus_data_in
addr_mux: PC
memory: read

#ldh
load_extra: true
D_latch_upper: true
D_upper_mux: bus_data_in
addr_mux: PC
memory: read

#drf
D_latch_lower: true
D_lower_mux: bus_data_in
addr_mux: D
memory: read

#off
load_extra: true
D_latch_lower: true
D_latch_upper: true
D_lower_mux: ALU_low
D_upper_mux: ALU_hi

:(
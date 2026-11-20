.syntax unified
.arm
.section .text
.global _start
_start:
 b boot
 .space 0x9c
 .ascii "POCKET QA   "
 .space 6
 .byte 0x96
 .space 13
boot:
 ldr r0, =0x04000000
 ldr r1, =0x0403
 strh r1, [r0]
 ldr r0, =0x06000000
 ldr r1, =0x03e0
 ldr r2, =38400
fill:
 strh r1, [r0], #2
 subs r2, r2, #1
 bne fill
 ldr r0, =0x04000084
 mov r1, #0x80
 strh r1, [r0]
 ldr r0, =0x04000080
 ldr r1, =0x1177
 strh r1, [r0]
 ldr r0, =0x04000082
 mov r1, #2
 strh r1, [r0]
 ldr r0, =0x04000062
 ldr r1, =0xf380
 strh r1, [r0]
 ldr r0, =0x04000064
 ldr r1, =0x8700
 strh r1, [r0]
 ldr r0, =0x0e000000
 mov r1, #0x5a
 strb r1, [r0]
loop: b loop
 .ltorg
 .ascii "SRAM_V113"

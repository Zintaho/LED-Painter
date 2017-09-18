/* [마이크로프로세서설계]
 * 최종 프로젝트
 * 블루투스로 제어하는 LED 그림판
 * 2012920041 컴퓨터과학부 유진석
 */

#define F_CPU 16000000UL

#include <avr/io.h>
#include <util/delay.h>
#include <avr/interrupt.h>

#include <stdint.h>

#include "pictures.h"

#define ClearOutBit    PORTB &= ~(1<<PB1)  //0 output (CLOSE)
#define SetOutBit      PORTB |= (1<<PB1)   //1 output (OPEN)

/*BAUD : 103 (9600BPS)*/
#define BAUD_H			0
#define BAUD			103

unsigned char rx[4]; //Received Data {R, G, B, index}
unsigned char counterColor = 0; //Length of current data

RGB matrix[MAX] = {{0,0,0},};

/*asm("nop") means no operation*/

//0.4 us (check datasheet)
void Set0( void ) 
{
	SetOutBit;
	asm("nop");asm("nop");asm("nop");asm("nop");asm("nop"); 
	ClearOutBit;
}

//0.85 us (check datasheet)
void Set1( void ) 
{
	SetOutBit;
	asm("nop");asm("nop");asm("nop");asm("nop");asm("nop");asm("nop");asm("nop");asm("nop");asm("nop");asm("nop");asm("nop");
	ClearOutBit;
}

void Reset(void)
{
	ClearOutBit;
	_delay_us(60);
}


void SetCell(unsigned char R, unsigned char G, unsigned char B, unsigned char index)
{
	matrix[index].R = R;
	matrix[index].G = G;
	matrix[index].B = B;
}


void SetMatrix(RGB temp[MAX])
{
	int i=0;
	for(i = 0; i < MAX; i ++)
	{
		SetCell(temp[i].R,temp[i].G,temp[i].B,i);
	}
	
}

/*G, R, B (CHECK DATASHEET)*/
void PrintCell(unsigned char index)
{
	unsigned char dummy = 0b10000000;
	int i = 0;
	
	for(i = 0; i < 8 ; i++)
	{
		(matrix[index].G & dummy) == dummy ? Set1() : Set0();
		dummy = dummy >> 1;
	}
	dummy = 0b10000000;
	
	for(i = 0; i < 8 ; i++)
	{
		(matrix[index].R & dummy) == dummy ? Set1() : Set0();
		dummy = dummy >> 1;
	}
	dummy = 0b10000000;
	for(i = 0; i < 8 ; i++)
	{
		(matrix[index].B & dummy) == dummy ? Set1() : Set0();
		dummy = dummy >> 1;
	}
	dummy = 0b10000000;
}

void PrintMatrix(int max)
{
	int i;
	
	for(i = 0; i < max; i++)
	{
		PrintCell(i);
	}
	
}

void init()
{
	//DDRA = 0b11111111;	//for debug
	DDRB = 0b00000010;	//Data Transport Pin
	
	UCSR0A = 0b00000000;/*Usart Control & Status Register A (initialize)*/
	
	UCSR0B = 0b10011000;	/*Usart Control & Status Register B
	
							 *7 RXCIE0: RX Complete Interrupt Enable
							 *4 RXEN0 : RX Enable
							 *3 TXEN0 : TX Enable
							 *2 UCSZ02: Character Size (UCSZ01, UCSZ00 is at UCSR0C)
							 UCSZ02 UCSZ01 UCSZ00
							 0		1		1		8-bit
							 */
	
	UCSR0C = 0b00000110;	/*Usart Control & Status Register C

							 *2 UCSZ01: Character Size 1:0 (UCSZ02 is at UCSR0B)
							 *1 UCSZ00:
							 */
	
	UBRR0H = BAUD_H;			/*Usart Baud Rate Register 11:0 (15:12 reserved)*/
	UBRR0L = BAUD;
	
}

ISR(USART0_RX_vect)
{
	//PORTA = counterColor + 1; //for debug

	rx[counterColor++] = UDR0;
	if(counterColor == 4)
	{
		if(rx[3] == 64) //RESET CODE
		{
			SetMatrix(CLEAR);

			rx[3] = 0;
		}
		else
		{
			SetCell(rx[0],rx[1],rx[2],rx[3]);
		}
		
		counterColor = 0;
		Reset();
		PrintMatrix(MAX);
	}
}

int main(void)
{
	init();
	
	Reset();
	PrintMatrix(MAX);
	
	sei();					//Global Interrupt Enabled
	
	for(;;)
	{
		
	}
	
	return 0;
}
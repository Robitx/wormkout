#!/bin/bash
for i in C*; do
	mv "$i" "level.txt"
	java -jar DigitToBinary.jar
	mv "level.txt" "$i"
	mv level.bin "$i.bin"
done


for i in E*; do
	mv "$i" "level.txt"
	java -jar DigitToBinary.jar
	mv "level.txt" "$i"
	mv level.bin "$i.bin"
done


for i in Pi*; do
	mv "$i" "level.txt"
	java -jar DigitToBinary.jar
	mv "level.txt" "$i"
	mv level.bin "$i.bin"
done
#!/bin/bash
# for the first 80%
for i in {1..80}
do
	./setEmulatorPowerCapacity.sh $i
	sleep 3
done

# for the next 15%
for i in {81..95}
do
	./setEmulatorPowerCapacity.sh $i
	sleep 7
done

# for the last 5%
for i in {96..100}
do
	./setEmulatorPowerCapacity.sh $i
	sleep 15
done
LOG_DIR=/home/hpc/rzku/pacl022h/logs

rm -f logs/*
rm -f worker.jobscript*

echo "starting master"
qsub.tinygpu project/master.jobscript 

while [ ! -f $LOG_DIR/master ] 
do
	echo "waiting for master..."
	sleep 2
done

LOOP="a b c"

for current in $LOOP
do
        echo "starting worker"
	project/runWorker.sh
done

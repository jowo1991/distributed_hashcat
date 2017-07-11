PATH=$PATH:/home/hpc/rzku/pacl022h/project/jre1.8.0_131/bin

java -jar jmxterm-1.0-alpha-4-uber.jar --url $(cat logs/master):1099 -v verbose

# bean de.jowo.pspac:type=MasterControl
# get WorkerCount
# get ...

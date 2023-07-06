scriptsdir=$(cd $(dirname $0) && pwd)
[ -x ${scriptsdir}/client.sh ] || chmod u+x ${scriptsdir}/*.sh
bash ${scriptsdir}/client.sh 600 960 &

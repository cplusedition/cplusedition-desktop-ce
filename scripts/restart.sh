scriptsdir=$(cd $(dirname $0) && pwd)
bash ${scriptsdir}/shutdown.sh
sleep 1
bash ${scriptsdir}/debug.sh

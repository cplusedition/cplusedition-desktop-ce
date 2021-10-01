ps auwx | grep -v grep | egrep -i '/electron/electron\s+' | perl -pe 's~^\S+\s+(\d+)\s+.*$~$1~' | xargs -r kill
ps auwx | egrep -i standalone | egrep -v grep | perl -pe 's~^\S+\s+(\d+)\s+.*$~$1~' | xargs -r kill

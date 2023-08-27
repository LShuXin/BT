#!/bin/sh

#############################################################
# monitor server running                                    #
# restart server if detect it is not running                #
# monitor.sh must be in the same directory with the server    #
#############################################################
function sendSMS() {
    # HTTP_HOST="http://api.transformer.mogujie.org/sms/mandao"
    HTTP_HOST="http://api.transformer.mogujie.org/sms/channel1"
    AppKey='82785972c1939448'
    InterFaceKey='mandao'
    # UserId="1,2,3,4,5"
    UserId="1"
    # 子烨、蓝狐、罗宁
    Phone=("18806535140" "18657139120" "18668072662")
    HostName=`hostname`

    # ##*/：在${PWD}之后，使用 ##*/ 对路径进行操作。这是一种称为"参数扩展"的特殊语法。
    # ${PWD##*/}：这个部分将会从${PWD}中去掉路径中的所有字符，直到最后一个斜杠 /（包括这个斜杠）。
    # 这就导致只保留了路径中的最后一个目录名部分。
    # 例如，如果当前工作目录是 /home/user/documents，那么在执行完这段代码后，DirName 将包含字符
    # 串 "documents"。
    DirName=${PWD##*/}
    Content='[TeamTalk] '$HostName' '$DirName' crash 【蘑菇街】'

    # date: 这是一个命令，用于显示或设置系统的日期和时间。在这里，它用于获取当前日期和时间。
    # +%s: 这是date命令的一个格式选项。%s用于显示自 Unix 纪元（1970年1月1日00:00:00 UTC）以来的秒数。
    # 000: 在这里，额外添加了三个零，以将秒数转换为毫秒数。因为1秒 = 1000毫秒，所以将获取的秒数乘以1000就得到了以毫秒为单位的时间戳。
    CreateTime=`date +%s`000
    for i in ${Phone[@]}
    do
        curl $HTTP_HOST -d "appKey=$AppKey&interfaceKey=$InterFaceKey&userId=$UserId&phone=$i&msg=$Content&submitTime=$CreateTime" > /dev/null 2>&1
    done
}

function monitor() {
    if [ ! -e *.conf ]
    then
        echo "no config file"
        return
    fi

    # $$ 代表当前正在执行的脚本或进程的pid
    echo $$ > monitor.pid
    local dot="."
    while true
    do
        if [ -e server.pid ]; then
            # \033[32m: 这是 ANSI 转义序列的开始，用于设置文本颜色。在这里，\033 是八进制表示的转义字符，
            # [32m 表示将文本颜色设置为绿色。不同的颜色和样式可以通过不同的数字组合来实现。
            # \033[0m: 这是 ANSI 转义序列的结束，用于重置文本颜色和样式为默认值。
            echo -e "\033[32m $1 ==> START SUCCESSFUL ... \033[0m"

            while true
            do
                pid=`cat server.pid`  # get pid
                process_count=`ps aux|grep $1|grep $pid|wc -l`
                if [ $process_count == 0 ]
                then
                    # send a SMS
                    sendSMS
                    # add log
                    date >> restart.log
                    echo "server stopped, pid=$pid, process_cnt=$process_count" >> restart.log
                    # restart server
                    if [ "$2" == "log" ]; then
                        ./$1
                    else
                        ../daeml ./$1
                    fi
                fi
                sleep 15
            done
        else          
            printf "%s %s\r" "$1=>Wait for start" "$dot"
            dot+="."
        fi
        sleep 1
    done
    echo "$1=>Quit"
}

case $1 in
    login_server)
        monitor $1 $2
        ;;
    msg_server)
        monitor $1 $2
        ;;
    route_server)
        monitor $1 $2
        ;;
    http_msg_server)
        monitor $1 $2
        ;;
    db_proxy_server)
        monitor $1 $2
        ;;
    file_server)
        monitor $1 $2
        ;;
    push_server)
        monitor $1 $2
        ;;
    msfs)
        monitor $1 $2
        ;;
    test)
        sendSMS
        ;;
    *)
        echo "Usage: "
        echo "  ./monitor.sh (login_server|msg_server|route_server|http_msg_server|db_proxy_server|file_server|push_server|msfs|test ) [log]"
        ;;
esac

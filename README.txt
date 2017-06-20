 В файле поток с Payload type: DynamicRTP-Type-97 (97)
 VLC не может его вопсроизвести потому что в поток не указано какого он формата
 там должен быть трафик SDP чтобы понять в каком формате поток, но его нет в дампе
 The RTP clock frequency is read from the profile file if given; the default profile (RFC 1890) is used if not. The profile file contains lines with two fields each: the first is the numeric payload type, the second the clock frequency. The values read from the profile file are silently ignored if the -T flag is used.
 этого тоже нет, так что приходится запускать rtpplay с -T - он тогда берет таймстемпы пакетов из дампа в том порядке в котором они приходили
 ffplay  то же самое отвечает [rtp @ 0x7fb5a1045600] Unable to receive RTP payload type 97 without an SDP file describing it
 Работает
 какие-то типы за кмпами сидят
 сделал файлик параметров сессии вручную
 прикинув что там поток h.264
 cat file.sdp
v=0
o=- 0 0 IN IP4 192.168.66.239
s=No Name
c=IN IP4 192.168.66.239
t=0 0
a=tool:libavformat 55.7.100
m=video 33518 RTP/AVP 97
b=AS:200
a=rtpmap:97 H264/90000
a=fmtp:97 profile-level-id=1
 Воспроизвожу так:
rtpplay -v -T  -f ./save_slr_1_1497447722_7fc3600b3120-0-13c4-65014-55-4452dabc-55_1_35_VIDE.rtpdump  192.168.66.239/33518
 Проигрываю так:
ffplay  -protocol_whitelist file,http,https,tcp,tls,rtp,udp file.sdp
 файл rtpdump получен через wireshark :
Telephony -> RTP -> RTP Streams -> Export
 RTPTools берутся тут http://www.cs.columbia.edu/irt/software/rtptools/
 под винду есть бинарники, под линукс и мак собираются ./configure && make && make install
 http://www.cs.columbia.edu/irt/software/rtptools/download/rtptools-1.21.tar.gz
 код последней версии
 Запись в файл: ffmpeg -protocol_whitelist file,crypto,udp,rtp -i file.sdp -vcodec libx264 -acodec aac -y output.mp4
 В обоих случаях сначала запускаем ffmpeg/ffplat а потом rtpplay

RTP H264 stream pcap to file or stream to browser


 нашел пример готового кода на LUA для корректного извлечения h264 в видеофайл
 https://github.com/volvet/h264extractor
 В маке у меня он не может почему-то выходной файл открыть если запускаю wireshark не из терминала
 А так работает
 h264extractor/rtp_h264_extractor.lua надо положить в ~/.wireshark/plugins/
 если такой папки нет - создать
 grep h264 ~/.wireshark/preferences
h264.dynamic.payload.type: 97
 вот эта ерунда должна быть в настройках выставлена
 перезапустить wireshark, только предварительно надо поправить сам файл скрипта и указать абсолютный путь к файлу дампа:
grep io.open ~/.wireshark/plugins/rtp_h264_extractor.lua
        local fp = io.open("/Users/omawnakw/rtp_pcap_stream/shitstream.h264", "wb")
 Пото в wireshark в меню Tools  пускаешь экстрактор
 MacBook-Pro-Daniil:rtp_pcap_stream omawnakw$ ls -lh shitstream.h264
-rw-r--r--  1 omawnakw  staff   1.2M Jun 19 23:27 shitstream.h264
 MacBook-Pro-Daniil:rtp_pcap_stream omawnakw$ file shitstream.h264
shitstream.h264: JVT NAL sequence, H.264 video, baseline @ L 20


 [20 июня 2017 г., 04:01]:
Вроде ничего сложного в коде. Для фиксированного значения jitter делается буфер в его размер, на случай если пакеты придут в неправильном порядке. Пакеты в буфере упорядочиваются, берется рабочее тело. На всякий случай сравнивают чтобы количество тел было равно количеству sequence numbers. Если не равно - ошибка. Но такое может быть только если просрался кусок пакета что непонятно как может быть - он бы либо в дамп не попал и отбраковался драйвером сетевухи или в самом дампе был бы отмечен как поврежденный и наверное не распарсился бы нормально

Дальше обработка согласно https://tools.ietf.org/html/rfc3984

Реализованы Single NAL, FU-A и STAP-a

Видимо причина по которой нельзя просто взять и херануть payload в файл даже при изначально правильном порядке пакетов и Single NAL - заголовок NAL преобразовывается по маске а также между заголовками и данными вставляется разделитель

В RTP потоке разделителем служит сам заголовок. Под него отводятся первые два байта нагрузки RTP

Преобразование заголовка для FU-A:  nal_header = bit.bor(bit.band(fu_indicator, 0xe0), bit.band(fu_header, 0x1f))
Разделитель: fp:write("\00\00\00\01")

В общем-то вся эта прелесть в rfc и описана. В одном rtp пакете может быть как один абстрактный сетевой юнит (NAL) и тогда нагрузка всего одна, так и несколько - тогда нагрузок несколько и у каждой свой заголовок NAL. И заголовки NAL надо преобразоывать в заголовки h264

А еще наоборот бывает - когда один NAL приходится бить на несколько пакетов. Тогда это получается FU

Вот еще описание: https://stackoverflow.com/questions/11953385/java-nal-to-h264


as long as your packet source is sending in single NAL unit mode or non-interleaved mode you can extract the NAL units without any further processing from the packets and dump them to disk with 0x00,0x00,0x00,0x01 as divider between them....

Можно и просто тупо стримить имеющийся поток в сеть. Только адреса позаменять и порты. Есть плееры на javascript с веб сокетами - они сами преобразуют RTP в MP4

Но хрен его знает насколько быстро этот шайтан будет в браузере отрабатывать

вот например https://github.com/Streamedian/html5_rtsp_player/wiki/HTML5-RTSP-Player

https://habrahabr.ru/post/229243/ - а тут на WebRTC

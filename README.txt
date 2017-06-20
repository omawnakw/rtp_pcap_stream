[15.06.17, 19:28:36] Даниил Александрович Чубий: а где ты взял что это H.264?
[15.06.17, 19:29:45] Даниил Александрович Чубий: В файле поток с Payload type: DynamicRTP-Type-97 (97)
[15.06.17, 19:30:11] Даниил Александрович Чубий: VLC не может его вопсроизвести потому что в поток не указано какого он формата
[15.06.17, 19:34:24] Даниил Александрович Чубий: там должен быть трафик SDP чтобы понять в каком формате поток, но его нет в дампе
[15.06.17, 19:55:34] Даниил Александрович Чубий: The RTP clock frequency is read from the profile file if given; the default profile (RFC 1890) is used if not. The profile file contains lines with two fields each: the first is the numeric payload type, the second the clock frequency. The values read from the profile file are silently ignored if the -T flag is used.
[15.06.17, 19:56:42] Даниил Александрович Чубий: этого тоже нет, так что приходится запускать rtpplay с -T - он тогда берет таймстемпы пакетов из дампа в том порядке в котором они приходили
[15.06.17, 19:59:57] Даниил Александрович Чубий: ffplay  то же самое отвечает [rtp @ 0x7fb5a1045600] Unable to receive RTP payload type 97 without an SDP file describing it
[15.06.17, 20:06:35] Даниил Александрович Чубий: Работает
[15.06.17, 20:06:42] Даниил Александрович Чубий: какие-то типы за кмпами сидят
[15.06.17, 20:07:07] Даниил Александрович Чубий: сделал файлик параметров сессии вручную
[15.06.17, 20:07:15] Даниил Александрович Чубий: прикинув что там поток h.264
[15.06.17, 20:07:25] Даниил Александрович Чубий: cat file.sdp
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
[15.06.17, 20:07:41] Даниил Александрович Чубий: Воспроизвожу так:
rtpplay -v -T  -f ./save_slr_1_1497447722_7fc3600b3120-0-13c4-65014-55-4452dabc-55_1_35_VIDE.rtpdump  192.168.66.239/33518
[15.06.17, 20:07:55] Даниил Александрович Чубий: Проигрываю так:
ffplay  -protocol_whitelist file,http,https,tcp,tls,rtp,udp file.sdp
[15.06.17, 20:08:50] Даниил Александрович Чубий: файл rtpdump получен через wireshark :
Telephony -> RTP -> RTP Streams -> Export
[15.06.17, 20:13:34] Даниил Александрович Чубий: RTPTools берутся тут http://www.cs.columbia.edu/irt/software/rtptools/
[15.06.17, 20:14:01] Даниил Александрович Чубий: под винду есть бинарники, под линукс и мак собираются ./configure && make && make install
[15.06.17, 20:14:44] Даниил Александрович Чубий: http://www.cs.columbia.edu/irt/software/rtptools/download/rtptools-1.21.tar.gz
[15.06.17, 20:14:51] Даниил Александрович Чубий: код последней версии
[15.06.17, 20:15:38] Даниил Александрович Чубий: Запись в файл: ffmpeg -protocol_whitelist file,crypto,udp,rtp -i file.sdp -vcodec libx264 -acodec aac -y output.mp4
[15.06.17, 20:16:18] Даниил Александрович Чубий: В обоих случаях сначала запускаем ffmpeg/ffplat а потом rtpplay

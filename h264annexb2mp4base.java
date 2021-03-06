package com.googlecode.mp4parser.muxformats;

import org.mp4parser.Container;
import org.mp4parser.muxer.FileDataSourceImpl;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.tracks.h264.H264TrackImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created with IntelliJ IDEA.
 * User: magnus
 * Date: 2012-04-23
 * Time: 12:21
 * To change this template use File | Settings | File Templates.
 */
public class H264Example {
    public static void main(String[] args) throws IOException {
        H264TrackImpl h264Track = new H264TrackImpl(new FileDataSourceImpl("shitstream.h264"));
        Movie m = new Movie();
        m.addTrack(h264Track);

        //m.addTrack(aacTrack);

        {
            Container out = new DefaultMp4Builder().build(m);
            FileOutputStream fos = new FileOutputStream(new File("shitstream.mp4"));
            FileChannel fc = fos.getChannel();
            out.writeContainer(fc);
            fos.close();
        }
    }
}

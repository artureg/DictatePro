
@echo on
javac -d d:\.projects\.devacon\devacon-repo\devacon\Android\SoundRecorder\bin d:\.projects\.devacon\devacon-repo\devacon\Android\SoundRecorder\src\com\wiseapps\davacon\speex\SpeexWrapper.java

cd bin

javah -d d:\.projects\.devacon\devacon-repo\devacon\Android\SoundRecorder com.wiseapps.davacon.speex.SpeexWrapper
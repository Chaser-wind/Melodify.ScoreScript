# Melodify.ScoreScript

能够将以指定形式描述的乐谱编译成一段音乐的程序。该程序可以支持多种律制，可以使用多种音源，且用户可以自定义支持的律制及音源。

## 输入/输出

- 律制  
  任何一种抽象的律制需要给出的信息包括两方面，即相对音高关系以及绝对音高。  
  对于相对音高关系，我们以音分为单位给出其每周期的声音频率（对于现行的律制这个值是1200），而对于每周期中的每个音，需要给出它相对于基准音的频率倍率（单位也是音分），由此我们也需要给出一个基准音（这里需要注意的是，并非所有律制的基准音都是C，如五度相生法的基准音是D）。  
  对于绝对音高关系，我们需要给出一个参考音名以及它所在的周期，并给出它的标称频率（对于现行的律制，音名为A，周期为4，标称频率为440.0Hz）。  
  实际的文件格式如下：  
```json
{
  "name": "Just Intonation",               // 可在乐谱文件中引用的律制名称
  "refNoteName": "A",                      // 参考音的音名
  "refNotePeriod": 4,                      // 参考音所在的周期
  "refFrequency": 440.0,                   // 参考音的标称频率
  "notesInPeriod":                         // 每个周期中的所有音的信息
  [
    { 
      "name": "C",                         // 可于外部引用的音名
      "multiInCents": 0                    // 相对基准音的频率倍率
    },
    { "name": "D", "multiInCents": 204},
    { "name": "E", "multiInCents": 386},
    { "name": "F", "multiInCents": 498},
    { "name": "G", "multiInCents": 702},
    { "name": "A", "multiInCents": 884},
    { "name": "B", "multiInCents": 1088}
  ],
  "periodMultiInCents": 1200,              // 每周期的频率倍率
  "baseNoteName": "C"                      // 基准音的音名
}
```
  程序自带的律制为纯律（`Just Intonation`），五度相生律（`Pythagorean Tuning`），平均律（`Equal Temperament`），见`data/Temperament.json`。

- 音源 
  每种音源可以存放于一个目录中，目录包含两个文件，其一为`sound.wav`文件，其中包含所有的音源片段。其二为元信息，其中包含若干个值组，其格式如下所示：  
```json
{
  "startTimeInMS": 2603,  // 此音源在sound.wav中的起始时间(ms)
  "endTimeInMS": 5206,    // 此音源在sound.wav中的结束时间(ms)
  "frequency": 69.295658, // 此音源的标称频率
  "extendable": false,    // 此音源是否可延长
  "extStartTimeInMS": 0,  // 进行延长时使用的重复片段在sound.wav中的起始时间(ms)
  "extEndTimeInMS": 0     // 进行延长时使用的重复片段在sound.wav中的结束时间(ms)
}
```
  即`sound.wav`文件中从`startTimeInMS`到`endTimeInMS`之间的音频是频率为`frequency`的音源，而`extStartTimeInMS`到`extEndTimeInMS`之间的音频是需要延音时使用的重复片段。  
  程序在载入某目录下的所有音源文件时，首先读取`SoundSources.json`以获取音源信息。其格式如下：
```json
[
  { 
    "name": "piano", // 在索引中使用的音源名称
    "path": "piano"  // 音源所在的目录名，若省略则使用音源名称作为目录名
  }
  // ....
]
```
  此处需要注意的是，若出现多个名称重复的音源，程序将使用出现的第一个。  
  程序自带的音源为钢琴(`piano`)，吉他(`guitar`)，正弦信号(`sine`)，见`data/sounds`

- 乐谱
  定义一种语言以表示乐谱，其中需包含多个音轨。每个音轨可能包含的信息为：调式（用于支持用唱名而非音名编写，也可以不支持），BPM，律制，音源名称与实际的乐谱等。实际的项目中采用两种不同的文件格式来表示之，其中一种为可让使用者自行编写的，容易阅读的文本格式（未实现，程序现在只能读取元信息），另一种为程序实际使用的元信息。程序在运行时首先预编译使用者编写的代码以生成元信息，之后读入元信息以获取乐谱信息。每个元信息文件包含每分钟的拍数，律制名称以及音轨的集合（数组），每个音轨需要包含其音源的名称以及对应的音符集合（数组）。每个音符需要包含其名称，所在周期，出现的时间以及持续的拍数。如下所示（若不标明，以下的域都是不能省略的）：  
```json
{
  "name": "Little Star",              // 乐谱的名称
  "bpm": 80,                          // 每分钟的节拍数
  "temperament": "Equal Temperament", // 使用的律制名称
  "scores":                           // 音轨集合
  [
    {
      "soundSource": "piano",         // 此音轨的音源名称
      "notes":                        // 此音轨的声音信息，即音符集合
      [
        {
          "name": "G",                // 此音符的音名
          "period": 2,                // 此音符所在的周期数
          "position": 0,              // 此音符出现的时间（单位为拍）
          "length": -1                // 此音符应当持续的拍数（-1或省略代表直接使用音源）
        },
        { "name": "C", "period": 3, "position": 0 }
        // ...
      ]
    }
  ]
}
```

- 运行时的程序输入/输出  
  输入一个文件名`[filename]`，它将作为乐谱输入。同时对于与之在同一目录下的`sounds`子目录，程序将其中的文件夹作为音源读入（如果需要），而`Temperament.json`则将作为律制输入（如果需要）。程序对于输入的乐谱，输出一段音乐文件。

- 命令行参数  

  * `-m(--metadata)`（预编译模块未实现，当前版本的程序将默认启动此参数）
    将输入的乐谱文件视为元信息，不执行预编译。

  * `-o [outFile]`   
    指定输出文件名为`[outFile]`，若不指定，默认名称`[name].wav`，`[name]`为乐谱文件中的`name`域的值。  

  * `-s(--sound) [dir1];[dir2];...`  
    程序将在`[dir1]/`、`[dir2]/`等文件夹中搜索音源文件。这里可以指定多个目录名称，中间须以分号分隔。  

  * `-t(--temperament) [file1];[file2];...`  
    程序将会将`[file1]`、`[file2]`等文件作为律制文件输入。  

  * 程序寻找带有指定名称的音源/律制的逻辑是，若指定了命令行参数，则首先在命令行参数指定的位置寻找，之后试图在`sounds`子目录和`Temperament.json`中寻找，之后在程序目录下的`data`文件夹中寻找。一旦找到立即返回。如果在所有位置都无法找到对应的对象，则报错。

  * 命令行参数中若含有空格，应以双引号将之括起。文件名中**不应**含有分号，文件名中的目录分隔符必须使用斜线(`/`)，使用反斜线(`\`)会造成程序错误！

## 程序基本逻辑

对于输入的乐谱文件，程序首先执行预编译，将之转换成可用的元信息（或者对于`-m`，直接将之作为元信息读入），之后读入元信息，将其中的音符按照时间序列叠加（对于同一个音轨在同一时刻有多个音符的情况，将视情况调整音量，对于指定了音符持续的时长的情况，进行延音）后进行响度均衡并输出为声音文件。

为了实现指定的律制，我们需要为每一种指定的音源产生指定频率的声音。我们的做法是，在给定一个频率之后，在音源文件中寻找一个频率与之最接近的音源，并使用线性插值进行重取样后将之压缩以完成变频。若指定了时长，且音源支持延长，则利用重复片段进行延长。

## 使用此程序包

程序的开发环境是Oracle JDK1.8，若要运行此程序，必须安装Oracle JDK1.8，并将`java`、`javac`加入Path环境变量。

程序的主类是`com.github.ShiftAC.Melodify.ScoreScript.Main`。

在Linux下可以直接使用。

在Windows下使用时需要额外安装并加入path的程序为`make`与`rm`([下载地址](https://sourceforge.net/projects/unxutils/?source=typ_redirect))

- 编译：`make`

- 清理：`make clean`

为了方便使用，此程序已经编译。

## 示例及其输出结果

`data/script`文件夹下为测试程序

使用方法： `make run [args]`

`[args]`的可能取值为：

- `ARGS="\"data/scripts/Scale.json\""`  
  程序将编译`data/scripts/Scale.json`，产生音乐文件`Scale.wav`。
  此测试项可以用以测试程序包自带的音源与律制：可以通过修改文件支持本程序包自带的所有音源及律制。

- `ARGS="\"data/scripts/Little Star.json\""`  
  程序将编译`data/scripts/Little Star.json`，产生音乐文件`Little Star.wav`。
  此测试项可以用以测试程序包自带的音源与律制：可以通过修改文件支持本程序包自带的所有音源及律制。
- `ARGS="\"data/scripts/Bloom of Youth.json\""`  
  程序将编译`data/scripts/Bloom of Youth.json`，产生音乐文件`Bloom of Youth.wav`。
  此测试项用于展示本程序包生成的实际音乐。此测试项不支持纯律（因为纯律中不包含半音阶）。
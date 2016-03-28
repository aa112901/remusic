# remusic
仿网易云音乐安卓版客户端(求offer)

#screenshot

![](https://github.com/aa112901/remusic/blob/master/remusic/screenshot/device-2016-03-24-133321%20(%E5%A4%8D%E5%88%B6).png) ![](https://github.com/aa112901/remusic/blob/master/remusic/screenshot/device-2016-03-24-133341%20(%E5%A4%8D%E5%88%B6).png)
![](https://github.com/aa112901/remusic/blob/master/remusic/screenshot/device-2016-03-24-133544%20(%E5%A4%8D%E5%88%B6).png)
![](https://github.com/aa112901/remusic/blob/master/remusic/screenshot/play_change.png)
![](https://github.com/aa112901/remusic/blob/master/remusic/screenshot/device-2016-03-24-134104%20(%E5%A4%8D%E5%88%B6).png)
![](https://github.com/aa112901/remusic/blob/master/remusic/screenshot/device-2016-03-24-151931%20(%E5%A4%8D%E5%88%B6).png)
![](https://github.com/aa112901/remusic/blob/master/remusic/screenshot/device-2016-03-24-134324%20(%E5%A4%8D%E5%88%B6).png)
![](https://github.com/aa112901/remusic/blob/master/remusic/screenshot/device-2016-03-26-121911%20(%E5%A4%8D%E5%88%B6).png)
![](https://github.com/aa112901/remusic/blob/master/remusic/screenshot/device-2016-03-26-123513.png)


aidl
│  └─com
│      └─wm
│          └─remusic
│              │  MediaAidlInterface.aidl  aidl文件
│              │  
│              └─service
│                      MusicTrack.aidl      aidl传输类的定义文件
│                      
├─java
│  └─com
│      └─wm
│          └─remusic
│              │  MainApplication.java    application类
│              │  
│              ├─activity
│              │      BaseActivity.java   基类activity
│              │      FirstScreen.java    首屏
│              │      LoadingActivity.java   启动activi
│              │      MainActivity.java   主activity
│              │      PlayingActivity.java   播放中activity
│              │      PlaylistManagerActivity.java   播放列表管理
│              │      PlaylistSelectActivity.java    播放列表选择
│              │      SearchActivity.java    搜索界面
│              │      SelectActivity.java    歌曲搜索activity
│              │      
│              ├─adapter
│              │      MainFragmentAdapter.java 主frament适配器
│              │      MainFragmentItem.java   主fragmnet的上部item信息
│              │      MusicFlowAdapter.java   歌曲弹出菜单适配器
│              │      OverFlowAdapter.java    专辑歌手等通用弹出菜单适配器
│              │      OverFlowItem.java       通用弹出列表信息项
│              │      PlaylistDetailAdapter.java  播放列表详细信息列表适配器
│              │      SearchAdapter.java       搜索列表适配器
│              │      
│              ├─dialog
│              │      AddPlaylistDialog.java   播放列表收藏
│              │      
│              ├─fragment
│              │      AlbumDetailFragment.java   专辑详细浏览
│              │      AlbumFragment.java           专辑浏览
│              │      ArtistDetailFragment.java    歌手详细列表项浏览
│              │      ArtistFragment.java          歌手浏览
│              │      BaseFragment.java            基类
│              │      FolderDetailFragment.java    文件夹详细浏览
│              │      FolderFragment.java          文件夹浏览
│              │      MainFragment.java            主frament显示主界面
│              │      MoreFragment.java            歌曲详细信息与专辑详细信息fragment
│              │      MusicDetailFragment.java     歌曲详细描述fragment
│              │      MusicFragment.java           歌曲界面
│              │      PlaylistDetailFragment.java  播放列表详细项
│              │      PlayQueueFragment.java       播放队列
│              │      RecentFragment.java          最近播放
│              │      RoundFragment.java           播放界面的圆形专辑
│              │      TabPagerFragment.java        滑动切换歌曲、专辑。歌手等viewpager fragment
│              │      TimingFragment.java          定时播放
│              │      
│              ├─handler 
│              │      HandlerUtil.java             通用handler
│              │      UnceHandler.java             捕获全局异常handler
│              │       
│              ├─info
│              │      AlbumInfo.java                专辑信息
│              │      ArtistInfo.java               歌手信息
│              │      FolderInfo.java               文件夹信息
│              │      MusicInfo.java                歌曲信息
│              │      Playlist.java                 播放列表信息
│              │        
│              ├─lastfmapi                          lastfm 获取歌手图片
│              │  │  LastFmClient.java              客户端
│              │  │  LastFmRestService.java          service
│              │  │  RestServiceFactory.java         servicefactory
│              │  │  
│              │  ├─callbacks
│              │  │      AlbuminfoListener.java     专辑信息监听
│              │  │      ArtistInfoListener.java    歌手信息监听
│              │  │      
│              │  └─models                       各种信息类
│              │          AlbumBio.java         
│              │          AlbumInfo.java
│              │          AlbumQuery.java
│              │          AlbumTracks.java
│              │          ArtistBio.java
│              │          ArtistInfo.java
│              │          ArtistQuery.java
│              │          ArtistTag.java
│              │          Artwork.java
│              │          LastfmAlbum.java
│              │          LastfmArtist.java
│              │           
│              ├─permissions                 权限辅助类
│              │      Nammu.java
│              │      PermissionCallback.java
│              │      PermissionListener.java
│              │      PermissionRequest.java
│              │       
│              ├─provider                     数据库操作类
│              │      MusicDB.java            
│              │      MusicPlaybackState.java  播放队列管理
│              │      PlaylistInfo.java        播放列表管理
│              │      PlaylistsManager.java     播放列表的歌曲管理
│              │      RecentStore.java          最近播放管理
│              │      SearchHistory.java        搜索历史管理
│              │      
│              ├─receiver
│              │      LaunchNowPlayingReceiver.java   打开playingactivity
│              │      MediaButtonIntentReceiver.java   接受按键信息
│              │      
│              ├─recent                          最近播放统计
│              │      PlayQueueCursor.java       播放队列cusor
│              │      QueueLoader.java           播放队列获取
│              │      Song.java                  歌曲信息类
│              │      SongLoader.java            歌曲获取
│              │      SongPlayCount.java         歌曲播放统计
│              │      SortedCursor.java          排序cursor
│              │      TopTracksLoader.java       最多播放获取
│              │      
│              ├─service
│              │      MediaService.java           播放service
│              │      MusicPlayer.java            播放辅助类
│              │      MusicTrack.java             aidl传输数据
│              │      
│              └─uitl
│                      AlpnaView.java
│                      AlpnaViewSelecterListener.java
│                      BuildProperties.java      获取系统信息
│                      CircleImageView.java      圆形图片
│                      CommonUtils.java          通用工具类
│                      ConverPinYin.java          拼音类
│                      DividerItemDecoration.java  列表分隔符
│                      DragSortRecycler.java     拖拽排序
│                      IConstants.java           常量
│                      ImageUtils.java            虚化，转换等util
│                      MusicUtils.java            数据库查询util
│                      PreferencesUtility.java    通用preferences
│                      RoundImageView.java         自定义view，实现唱片
│                      SearchUtils.java            搜索util
│                      SortOrder.java              查询的排序
│                      StringHelper.java            String帮助类

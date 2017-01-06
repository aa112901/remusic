//package com.wm.remusic.downmusic;
//
//import android.content.Context;
//import android.util.Log;
//import android.util.SparseArray;
//
//import com.squareup.okhttp.OkHttpClient;
//import com.wm.remusic.provider.DownFileStore;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//
///**
// * Created by dzc on 15/11/21.
// */
//public class DownloadManager implements DownloadTaskListener{
//    private static final String TAG = "DownloadManager";
//    private Context context;
//    private static DownloadManager downloadManager;
//    private static DownFileStore downFileStore;
//    private int mPoolSize = 1;
//    private ExecutorService executorService;
//    private Map<String, Future> futureMap;
//    private OkHttpClient client;
//    private List<DownloadDBEntity> allTaskEntity = new ArrayList<>();
//    private ArrayList<DownloadTask> prepareTaskList = new ArrayList<>();
//    private DownloadTask currentTask;
//
//    public Map<String, DownloadTask> getCurrentTaskList() {
//        return currentTaskList;
//    }
//
//    private Map<String, DownloadTask> currentTaskList = new HashMap<>();
//
//    public void init() {
//        executorService = Executors.newFixedThreadPool(mPoolSize);
//        futureMap = new HashMap<>();
//        downFileStore = DownFileStore.getInstance(context);
//        client = new OkHttpClient();
//        allTaskEntity = loadAllDownloadEntityFromDB();
//    }
//
//    private DownloadManager() {
//        init();
//    }
//
//    private DownloadManager(Context context) {
//        this.context = context;
//        init();
//    }
//
//    public static DownloadManager getInstance(Context context) {
//        if (downloadManager == null) {
//            downloadManager = new DownloadManager(context);
//        }
//        return downloadManager;
//    }
//
//    public synchronized void addDownloadTask(DownloadTask task, DownloadTaskListener listener) {
//        if (null != currentTaskList.get(task.getId()) && task.getDownloadStatus() != DownloadStatus.DOWNLOAD_STATUS_INIT) {
//            Log.d(TAG, "task already exist");
//            return;
//        }
//        DownloadDBEntity dbEntity = new DownloadDBEntity(task.getId(), task.getTotalSize(),
//                task.getCompletedSize(), task.getUrl(), task.getSaveDirPath(), task.getFileName(),task.getArtistName(), task.getDownloadStatus());
//        downFileStore.insert(dbEntity);
//        task.setPreparingDown(true);
//        allTaskEntity.add(dbEntity);
//        prepareTaskList.add(task);
//        currentTaskList.put(task.getId(), task);
//        if(currentTask != null){
//            return;
//        }
//
//        task.setDownloadStatus(DownloadStatus.DOWNLOAD_STATUS_PREPARE);
//        task.setdownFileStore(downFileStore);
//        task.setHttpClient(client);
//        task.addDownloadListener(this);
//        if (listener != null) {
//            task.addDownloadListener(listener);
//        }
//        Future future = executorService.submit(task);
//        futureMap.put(task.getId(), future);
//        currentTask = task;
//
//    }
//
//
//   public void startTask(){
//       if(prepareTaskList.size() > 0){
//           DownloadTask downloadTask = prepareTaskList.get(0);
//           if (downloadTask.getDownloadStatus() != DownloadStatus.DOWNLOAD_STATUS_COMPLETED) {
//                downloadTask.setDownloadStatus(DownloadStatus.DOWNLOAD_STATUS_PREPARE);
//                downloadTask.setdownFileStore(downFileStore);
//                downloadTask.setHttpClient(client);
//                Future future = executorService.submit(downloadTask);
//                futureMap.put(downloadTask.getId(), future);
//            }
//       }
//   }
//
//    /**
//     * if return null,the task does not exist
//     *
//     * @param taskId
//     * @return
//     */
//    public void resume(String taskId) {
//        DownloadTask downloadTask = getCurrentTaskById(taskId);
//        if(downloadTask == null){
//            downloadTask = getDBTaskById(taskId);
//        }
//        prepareTaskList.add(downloadTask);
//        if(currentTask == null){
//            startTask();
//        }
//
////        if (downloadTask != null) {
////            if (downloadTask.getDownloadStatus() != DownloadStatus.DOWNLOAD_STATUS_COMPLETED) {
////                downloadTask.setDownloadStatus(DownloadStatus.DOWNLOAD_STATUS_PREPARE);
////                downloadTask.setdownFileStore(downFileStore);
////                downloadTask.setHttpClient(client);
////                Future future = executorService.submit(downloadTask);
////                futureMap.put(downloadTask.getId(), future);
////            }
////
////        } else {
////            downloadTask = getDBTaskById(taskId);
////            if (downloadTask != null) {
////                currentTaskList.put(taskId, downloadTask);
////                downloadTask.setDownloadStatus(DownloadStatus.DOWNLOAD_STATUS_PREPARE);
////                downloadTask.setdownFileStore(downFileStore);
////                downloadTask.setHttpClient(client);
////                Future future = executorService.submit(downloadTask);
////                futureMap.put(downloadTask.getId(), future);
////            }
////        }
////        return downloadTask;
//    }
//
//
////    /**
////     * if return null,the task does not exist
////     * @param taskId
////     * @return
////     */
////    public void resume(String taskId){
//////        Log.e("taskid",taskId);
//////        DownloadTask downloadTask = getCurrentTaskId(taskId);
//////        if(downloadTask!=null){
//////            Log.e("taskid","is exits in current");
//////            Log.e("task status",downloadTask.getDownloadStatus() + "");
//////            if(downloadTask.getDownloadStatus()!=DownloadStatus.DOWNLOAD_STATUS_COMPLETED){
//////                Log.e("taskid","to run");
//////                Future future =  executorService.submit(downloadTask);
//////                Log.e("taskid","run");
//////                futureMap.put(downloadTask.getId(),future);
//////            }
//////
//////        }else{
////
////      DownloadTask downloadTask = getDBTaskById(taskId);
////
////            if(downloadTask!=null){
////                currentTaskList.put(taskId,downloadTask);
////                Future future =  executorService.submit(downloadTask);
////                futureMap.put(downloadTask.getId(),future);
////            }
//// //       }
////  //      return downloadTask;
////    }
//
//
//    public void addDownloadListener(DownloadTask task, DownloadTaskListener listener) {
//        task.addDownloadListener(listener);
//    }
//
//    public void removeDownloadListener(DownloadTask task, DownloadTaskListener listener) {
//        task.removeDownloadListener(listener);
//    }
//
//    public void addDownloadTask(DownloadTask task) {
//        addDownloadTask(task, null);
//    }
//
//    public void cancel(DownloadTask task) {
//        task.cancel();
//        currentTaskList.remove(task.getId());
//        prepareTaskList.remove(task);
//        futureMap.remove(task.getId());
//        task.setDownloadStatus(DownloadStatus.DOWNLOAD_STATUS_CANCEL);
//        downFileStore.deleteTask(task.getId());
//    }
//
//    public void cancel(String taskId) {
//        DownloadTask task = getTaskById(taskId);
//        if (task != null) {
//            cancel(task);
//        }
//    }
//
//    public void pause(DownloadTask task) {
//        task.pause();
//        currentTaskList.remove(task.getId());
//        futureMap.remove(task.getId());
//    }
//
//    public void pause(String taskId) {
//        DownloadTask task = getTaskById(taskId);
//        if (task != null) {
//            pause(task);
//        }
//    }
//
//
//    private List<DownloadDBEntity> loadAllDownloadEntityFromDB() {
//        return downFileStore.getDownLoadedListAll();
//    }
//
//    public List<DownloadTask> loadAllDownloadTaskFromDB() {
//        List<DownloadDBEntity> list = loadAllDownloadEntityFromDB();
//        List<DownloadTask> downloadTaskList = null;
//        if (list != null && !list.isEmpty()) {
//            downloadTaskList = new ArrayList<>();
//            for (DownloadDBEntity entity : list) {
//                downloadTaskList.add(DownloadTask.parse(entity, context));
//            }
//        }
//        return downloadTaskList;
//    }
//
//    public List<DownloadTask> loadDownloadingTaskFromDB() {
//        List<DownloadDBEntity> list = loadAllDownloadEntityFromDB();
//        List<DownloadTask> downloadTaskList = null;
//        if (list != null && !list.isEmpty()) {
//            downloadTaskList = new ArrayList<>();
//            for (DownloadDBEntity entity : list) {
//                if (entity.getCompletedSize().equals(entity.getTotalSize())) {
//                    continue;
//                }
//
//                downloadTaskList.add(DownloadTask.parse(entity, context));
//            }
//        }
//        return downloadTaskList;
//    }
//
//    public List<DownloadTask> loadDownloadCompletedTaskFromDB() {
//        List<DownloadDBEntity> list = loadAllDownloadEntityFromDB();
//        List<DownloadTask> downloadTaskList = null;
//        if (list != null && !list.isEmpty()) {
//            downloadTaskList = new ArrayList<>();
//            for (DownloadDBEntity entity : list) {
//                if (entity.getCompletedSize().equals(entity.getTotalSize()))
//                    downloadTaskList.add(DownloadTask.parse(entity, context));
//            }
//        }
//        return downloadTaskList;
//    }
//
//    public DownloadTask getCurrentTaskById(String taskId) {
//        for(int i = 0 ; i< prepareTaskList.size(); i++){
//            if(prepareTaskList.get(i).getId() == taskId){
//                return prepareTaskList.get(i);
//            }
//        }
//        return null;
//       // return currentTaskList.get(taskId);
//    }
//
//    public boolean isCurrentTask(String taskId) {
//        DownloadTask task = null;
//        task = getCurrentTaskById(taskId);
//        if (task != null) {
//            return true;
//        }
//        return false;
//    }
//
//    public DownloadTask getTaskById(String taskId) {
//        DownloadTask task = null;
//        task = getCurrentTaskById(taskId);
//        if (task != null) {
//            return task;
//        }
//        return getDBTaskById(taskId);
//    }
//
//    public DownloadTask getDBTaskById(String taskId) {
//        DownloadDBEntity entity = downFileStore.getDownLoadedList(taskId);
//        if (entity != null) {
//            return DownloadTask.parse(entity, context);
//        }
//        return null;
//    }
//
//    @Override
//    public void onPrepare(DownloadTask downloadTask) {
//
//    }
//
//    @Override
//    public void onStart(DownloadTask downloadTask) {
//
//    }
//
//    @Override
//    public void onDownloading(DownloadTask downloadTask) {
//
//    }
//
//    @Override
//    public void onPause(DownloadTask downloadTask) {
//        if(prepareTaskList.size() > 0){
//            prepareTaskList.remove(currentTask);
//        }
//        currentTask = null;
//        startTask();
//    }
//
//    @Override
//    public void onCancel(DownloadTask downloadTask) {
//        if(prepareTaskList.size() > 0){
//            prepareTaskList.remove(currentTask);
//        }
//        currentTask = null;
//        startTask();
//    }
//
//    @Override
//    public void onCompleted(DownloadTask downloadTask) {
//        if(prepareTaskList.size() > 0){
//            prepareTaskList.remove(currentTask);
//        }
//        currentTask = null;
//        startTask();
//
//    }
//
//    @Override
//    public void onError(DownloadTask downloadTask, int errorCode) {
//        startTask();
//    }
//
//
//}

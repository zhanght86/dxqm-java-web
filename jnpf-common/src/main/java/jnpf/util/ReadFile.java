package jnpf.util;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Java读取文件夹下的所有文件名和文件
 * @author yinmaisoft
 *
 */
@Slf4j
public class ReadFile {

    /**
     * 预览代码
     * @param codePath
     * @return
     */
    public static List<Map<String,Object>> priviewCode(String codePath) {
        List<String> paths= traverseFolder1(codePath);
        List<Map<String,String>> allDatas=new ArrayList<>();
        List<Map<String,Object>> allTreeDatas=new ArrayList<>();
        for(String path:paths){
            File file=new File(path);
            if(file.isFile()){
                Map<String,String> data=new HashMap<>(16);
                try {//读取指定文件路径下的文件内容
                    String fileDatas = readFile(file);
                    String os = System.getProperty("os.name");
                    if(os.toLowerCase().startsWith("win")){
                        String[] fileNames=path.split("\\\\");
                        if("java".equals(fileNames[fileNames.length-1].split("\\.")[1])){
                            String fileName=fileNames[fileNames.length-1].toLowerCase();
                            if(fileName.contains("crform")||fileName.contains("upform")
                                    ||fileName.contains("listvo")||fileName.contains("infovo")
                                    ||fileName.contains("listquery") ||fileName.contains("pagination")){
                                data.put("folderName","model");
                                data.put("fileName",fileNames[fileNames.length-1]);
                                data.put("fileContent",fileDatas);
                                data.put("fileType",fileNames[fileNames.length-1].split("\\.")[1]);
                                allDatas.add(data);
                            }else{
                                data.put("folderName","java");
                                data.put("fileName",fileNames[fileNames.length-1]);
                                data.put("fileContent",fileDatas);
                                data.put("fileType",fileNames[fileNames.length-1].split("\\.")[1]);
                                allDatas.add(data);
                            }
                        }else if(fileNames[fileNames.length-1].contains("vue")){
                            data.put("folderName","vue");
                            data.put("fileName",fileNames[fileNames.length-1]);
                            data.put("fileContent",fileDatas);
                            data.put("fileType",fileNames[fileNames.length-1].split("\\.")[1]);
                            allDatas.add(data);
                        }
                    }else{
                        String[] fileNames=path.split(File.separator);
                        if("java".equals(fileNames[fileNames.length-1].split("\\.")[1])){
                            String fileName=fileNames[fileNames.length-1].toLowerCase();
                            if(fileName.contains("crform")||fileName.contains("upform")
                                    ||fileName.contains("listvo")||fileName.contains("infovo")
                                    ||fileName.contains("listquery") ||fileName.contains("pagination")){
                                data.put("folderName","model");
                                data.put("fileName",fileNames[fileNames.length-1]);
                                data.put("fileContent",fileDatas);
                                data.put("fileType",fileNames[fileNames.length-1].split("\\.")[1]);
                                allDatas.add(data);
                            }else{
                                data.put("folderName","java");
                                data.put("fileName",fileNames[fileNames.length-1]);
                                data.put("fileContent",fileDatas);
                                data.put("fileType",fileNames[fileNames.length-1].split("\\.")[1]);
                                allDatas.add(data);
                            }
                        }else if(fileNames[fileNames.length-1].contains("vue")){
                            data.put("folderName","vue");
                            data.put("fileName",fileNames[fileNames.length-1]);
                            data.put("fileContent",fileDatas);
                            data.put("fileType",fileNames[fileNames.length-1].split("\\.")[1]);
                            allDatas.add(data);
                        }
                    }
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
        List<String> parents=allDatas.stream().map(t->t.get("folderName")).distinct().collect(Collectors.toList());
        for(String parent:parents){
            Map<String,Object> dataMap=new HashMap<>(16);
            dataMap.put("fileName" ,parent);
            dataMap.put("children" ,allDatas.stream().filter(t -> parent.equals(t.get("folderName"))).collect(Collectors.toList()));
            allTreeDatas.add(dataMap);
        }
        return allTreeDatas;
    }

    public static List<String> traverseFolder1(String path) {
        List<String> paths=new ArrayList<>();
        int fileNum = 0, folderNum = 0;
        File file = new File(path);
        if (file.exists()) {
            LinkedList<File> list = new LinkedList<File>();
            File[] files = file.listFiles();
            for (File file2 : files) {
                if (file2.isDirectory()) {
                    list.add(file2);
                    folderNum++;
                } else {
                    paths.add(file2.getAbsolutePath());
                    fileNum++;
                }
            }
            File tempFile;
            while (!list.isEmpty()) {
                tempFile = list.removeFirst();
                files = tempFile.listFiles();
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        list.add(file2);
                        folderNum++;
                    } else {
                        paths.add(file2.getAbsolutePath());
                        fileNum++;
                    }
                }
            }
        } else {
            log.error("文件不存在!");
        }
        return paths;
    }


    /**
     * 获取某文件夹下的文件名和文件内容,存入map集合中
     * @param filePath 需要获取的文件的 路径
     * @return 返回存储文件名和文件内容的map集合
     */
    public static Map<String, String> getFilesDatas(String filePath){
        Map<String, String> files = new HashMap<>(16);
        //需要获取的文件的路径
        File file = new File(filePath);
        //存储文件名的String数组
        String[] fileNameLists = file.list();
        //存储文件路径的String数组
        File[] filePathLists = file.listFiles();
        for(int i=0;i<filePathLists.length;i++){
            if(filePathLists[i].isFile()){
                try {//读取指定文件路径下的文件内容
                    String fileDatas = readFile(filePathLists[i]);
                    //把文件名作为key,文件内容为value 存储在map中
                    files.put(fileNameLists[i], fileDatas);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return files;
    }

    /**
     * 读取指定目录下的文件
     * @param path 文件的路径
     * @return 文件内容
     * @throws IOException
     */
    public static String readFile(File path) throws IOException{
        //创建一个输入流对象
        @Cleanup InputStream is=new FileInputStream(path);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int n ;
        while ((n = is.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        //释放资源
        is.close();
        return out.toString();
    }
}

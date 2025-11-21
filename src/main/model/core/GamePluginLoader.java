package model.core;

import java.awt.Component;
import java.io.*;
import java.lang.reflect.Modifier;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import javax.swing.*;


public class GamePluginLoader {
    private static GamePluginLoader instance;
    private Map<String, GamePlugin> loadedPlugins;
    private final String PLUGINS_DIR = "plugins";
    private WatchService watchService;
    private Thread watchThread;
    private Runnable onPluginAdded;
    private Set<String> loadedJarFiles;
    
    private GamePluginLoader() {
        this.loadedPlugins = new HashMap<>();
        this.loadedJarFiles = new HashSet<>();
    }
    
    public static GamePluginLoader getInstance() {
        if (instance == null) {
            instance = new GamePluginLoader();
        }
        return instance;
    }
    
    public void loadExternalGames() {
        try {
            File pluginsDir = new File(PLUGINS_DIR);
            
            if (!pluginsDir.exists()) {
                pluginsDir.mkdirs();
                System.out.println("Directorio plugins creado");
            }
            
            File[] jarFiles = pluginsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
            
            if (jarFiles == null || jarFiles.length == 0) {
                System.out.println("No se encontraron plugins en el directorio: " + PLUGINS_DIR);
                startDirectoryWatcher();
                return;
            }
            
            System.out.println("Encontrados " + jarFiles.length + " archivos JAR en plugins/");
            
            for (File jarFile : jarFiles) {
                try {
                    String jarName = jarFile.getName();
                    if (!loadedJarFiles.contains(jarName)) {
                        loadPluginFromJar(jarFile);
                        loadedJarFiles.add(jarName);
                    }
                } catch (Exception e) {
                    System.err.println("Error cargando plugin: " + jarFile.getName());
                    e.printStackTrace();
                }
            }
            
            System.out.println("Carga de plugins completada. Total: " + loadedPlugins.size());
            
            startDirectoryWatcher();
            
        } catch (Exception e) {
            System.err.println("Error en carga de plugins: " + e.getMessage());
        }
    }
    
    private void startDirectoryWatcher() {
        try {
            if (watchService != null) {
                return;
            }
            
            watchService = FileSystems.getDefault().newWatchService();
            Path pluginsPath = Paths.get(PLUGINS_DIR);
            
            if (!Files.exists(pluginsPath)) {
                Files.createDirectories(pluginsPath);
            }
            
            pluginsPath.register(watchService, 
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY);
            
            watchThread = new Thread(() -> {
                try {
                    while (true) {
                        WatchKey key = watchService.take();
                        
                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();
                            
                            if (kind == StandardWatchEventKinds.ENTRY_CREATE || 
                                kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                                
                                Path filePath = (Path) event.context();
                                String fileName = filePath.toString();
                                
                                if (fileName.toLowerCase().endsWith(".jar")) {
                                    File jarFile = new File(PLUGINS_DIR + File.separator + fileName);
                                    
                                    if (jarFile.exists() && !loadedJarFiles.contains(fileName)) {
                                        try {
                                            Thread.sleep(500);
                                            
                                            loadPluginFromJar(jarFile);
                                            loadedJarFiles.add(fileName);
                                            
                                            if (onPluginAdded != null) {
                                                SwingUtilities.invokeLater(onPluginAdded);
                                            }
                                            
                                        } catch (Exception e) {
                                            System.err.println("Error cargando nuevo plugin: " + fileName);
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                        
                        boolean valid = key.reset();
                        if (!valid) {
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            watchThread.setDaemon(true);
            watchThread.start();
            
        } catch (Exception e) {
            System.err.println("Error iniciando watcher: " + e.getMessage());
        }
    }
    
    public void setOnPluginAddedCallback(Runnable callback) {
        this.onPluginAdded = callback;
    }
    
    private void loadPluginFromJar(File jarFile) throws Exception {
        System.out.println("📦 Cargando plugin: " + jarFile.getName());
        
        URL jarUrl = jarFile.toURI().toURL();
        URLClassLoader classLoader = new URLClassLoader(
            new URL[]{jarUrl}, 
            this.getClass().getClassLoader()
        );
        
        try {
            ServiceLoader<GamePlugin> serviceLoader = ServiceLoader.load(GamePlugin.class, classLoader);
            
            int loadedCount = 0;
            for (GamePlugin plugin : serviceLoader) {
                String gameName = plugin.getGameName();
                
                if (!loadedPlugins.containsKey(gameName)) {
                    loadedPlugins.put(gameName, plugin);
                    System.out.println(" Plugin cargado: " + gameName + " v" + plugin.getGameVersion());
                    loadedCount++;
                } else {
                    System.out.println(" Plugin duplicado ignorado: " + gameName);
                }
            }
            
            if (loadedCount == 0) {
                System.out.println(" ServiceLoader no encontró plugins, intentando carga manual...");
                loadPluginsManually(jarFile, classLoader);
            }
        } catch (Exception e) {
            System.err.println("Error con ServiceLoader, usando carga manual: " + e.getMessage());
            loadPluginsManually(jarFile, classLoader);
        } finally {
            classLoader.close();
        }
    }
    

    private void loadPluginsManually(File jarFile, URLClassLoader classLoader) throws Exception {
        System.out.println("🔍 Intentando carga manual para: " + jarFile.getName());
        
        JarFile jar = new JarFile(jarFile);
        Enumeration<JarEntry> entries = jar.entries();
        
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            
            if (entry.getName().endsWith(".class") && !entry.getName().contains("$")) {
                String className = entry.getName()
                    .replace("/", ".")
                    .replace(".class", "");
                
                if (className.startsWith("META-INF") || className.contains("$")) {
                    continue;
                }
                
                try {
                    Class<?> clazz = classLoader.loadClass(className);
                    
                    if (GamePlugin.class.isAssignableFrom(clazz) && !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
                        GamePlugin plugin = (GamePlugin) clazz.getDeclaredConstructor().newInstance();
                        String gameName = plugin.getGameName();
                        
                        if (!loadedPlugins.containsKey(gameName)) {
                            loadedPlugins.put(gameName, plugin);
                            System.out.println(" Plugin cargado manualmente: " + gameName + " v" + plugin.getGameVersion());
                        }
                    }
                } catch (NoClassDefFoundError | Exception e) {
                    continue;
                }
            }
        }
        
        jar.close();
    }
    
    public List<GamePlugin> getLoadedPlugins() {
        return new ArrayList<>(loadedPlugins.values());
    }
    
    public boolean hasPlugins() {
        return !loadedPlugins.isEmpty();
    }

    public void loadPluginInteractive(Component parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar plugin JAR");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Archivos JAR", "jar"));
        fileChooser.setCurrentDirectory(new File(PLUGINS_DIR));
        
        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            try {
                File destFile = new File(PLUGINS_DIR + File.separator + selectedFile.getName());
                copyFile(selectedFile, destFile);
                
                loadPluginFromJar(destFile);
                
                JOptionPane.showMessageDialog(parent, 
                    "Plugin cargado exitosamente: " + selectedFile.getName(),
                    "Plugin Cargado", 
                    JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(parent, 
                    "Error cargando plugin: " + e.getMessage(),
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void copyFile(File source, File dest) throws IOException {
        try (InputStream is = new FileInputStream(source);
             OutputStream os = new FileOutputStream(dest)) {
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }
}


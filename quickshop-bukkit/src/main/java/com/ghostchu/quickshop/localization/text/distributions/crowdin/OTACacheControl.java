package com.ghostchu.quickshop.localization.text.distributions.crowdin;

import com.ghostchu.quickshop.util.Util;
import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.locks.ReentrantLock;

public class OTACacheControl {
    private final File metadataFile = new File(Util.getCacheFolder(), "i18n.metadata");
    private final YamlConfiguration metadata;
    private final ReentrantLock lock = new ReentrantLock();

    public OTACacheControl() {
        this.metadata = YamlConfiguration.loadConfiguration(this.metadataFile);
    }

    public boolean isCachedObjectOutdated(String path, long manifestTimestamp) {
        return readCachedObjectTimestamp(path) != manifestTimestamp;
    }

    public long readCachedObjectTimestamp(String path) {
        String cacheKey = hash(path);
        long l;
        lock.lock();
        try {
            l = this.metadata.getLong("objects." + cacheKey + ".time", -1);
        } finally {
            lock.unlock();
        }
        return l;
    }

    private String hash(String str) {
        return DigestUtils.sha1Hex(str);
    }

    public long readManifestTimestamp() {
        long l;
        lock.lock();
        try {
            l = this.metadata.getLong("manifest.timestamp", -1);
        } finally {
            lock.unlock();
        }
        return l;
    }

    public byte[] readObjectCache(String path) throws IOException {
        String cacheKey = hash(path);
        return Files.readAllBytes(new File(Util.getCacheFolder(), cacheKey).toPath());
    }

    public void writeManifestTimestamp(long timestamp) {
        lock.lock();
        try {
            this.metadata.set("manifest.timestamp", timestamp);
        } finally {
            lock.unlock();
        }

        save();
    }

    private void save() {
        lock.lock();
        try {
            this.metadata.save(this.metadataFile);
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void writeObjectCache(String path, byte[] data, long manifestTimestamp) throws IOException {
        String cacheKey = hash(path);
        Files.write(new File(Util.getCacheFolder(), cacheKey).toPath(), data);
        lock.lock();
        try {
            this.metadata.set("objects." + cacheKey + ".time", manifestTimestamp);
            this.metadata.set("objects." + cacheKey + ".path", path);
        } finally {
            lock.unlock();
            save();
        }

    }

}

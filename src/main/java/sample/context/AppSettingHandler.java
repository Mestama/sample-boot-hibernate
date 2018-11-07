package sample.context;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import sample.context.orm.SystemRepository;

/**
 * アプリケーション設定情報に対するアクセス手段を提供します。
 */
public class AppSettingHandler {

    @Autowired
    private SystemRepository rep;
    /** 設定時は固定のキー/値を返すモックモードとする */
    private final Optional<Map<String, String>> mockMap;

    public AppSettingHandler() {
        this.mockMap = Optional.empty();
    }

    public AppSettingHandler(Map<String, String> mockMap) {
        this.mockMap = Optional.of(mockMap);
    }

    /** アプリケーション設定情報を取得します。 */
    @Cacheable(cacheNames = "AppSettingHandler.appSetting", key = "#id")
    @Transactional(value = SystemRepository.BeanNameTx)
    public AppSetting setting(String id) {
        if (mockMap.isPresent())
            return mockSetting(id);
        AppSetting setting = AppSetting.load(rep, id);
        setting.hashCode(); // for loading
        return setting;
    }

    private AppSetting mockSetting(String id) {
        return new AppSetting(id, "category", "テスト用モック情報", mockMap.get().get(id));
    }

    /** アプリケーション設定情報を変更します。 */
    @CacheEvict(cacheNames = "AppSettingHandler.appSetting", key = "#id")
    @Transactional(value = SystemRepository.BeanNameTx)
    public AppSetting update(String id, String value) {
        return mockMap.isPresent() ? mockSetting(id) : AppSetting.load(rep, id).update(rep, value);
    }

}

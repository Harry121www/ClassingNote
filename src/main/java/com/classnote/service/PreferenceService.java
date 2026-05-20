package com.classnote.service;

import com.classnote.dto.PreferenceRequest;
import com.classnote.entity.UserPreference;
import com.classnote.repository.UserPreferenceRepository;
import org.springframework.stereotype.Service;

@Service
public class PreferenceService {

    private final UserPreferenceRepository prefRepo;

    public PreferenceService(UserPreferenceRepository prefRepo) {
        this.prefRepo = prefRepo;
    }

    public UserPreference get() {
        return prefRepo.findById("default").orElseGet(() -> prefRepo.save(new UserPreference()));
    }

    public UserPreference update(PreferenceRequest req) {
        UserPreference pref = get();
        if (req.getTheme() != null) pref.setTheme(req.getTheme());
        if (req.getAutoSaveInterval() != null) pref.setAutoSaveInterval(req.getAutoSaveInterval());
        if (req.getEditorFontSize() != null) pref.setEditorFontSize(req.getEditorFontSize());
        if (req.getAttachmentRootPath() != null) pref.setAttachmentRootPath(req.getAttachmentRootPath());
        if (req.getDataPath() != null) pref.setDataPath(req.getDataPath());
        return prefRepo.save(pref);
    }
}

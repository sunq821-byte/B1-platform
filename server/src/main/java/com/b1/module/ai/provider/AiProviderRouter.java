package com.b1.module.ai.provider;

import com.b1.module.submission.entity.SubmissionFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AiProviderRouter {

    private final DeepSeekProvider deepSeekProvider;
    private final QwenVisionProvider qwenVisionProvider;

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "png", "jpg", "jpeg", "gif", "bmp", "webp", "ico", "tiff", "tif"
    );

    private static final Set<String> CODE_EXTENSIONS = Set.of(
            "java", "py", "js", "ts", "vue", "jsx", "tsx", "go", "rs",
            "c", "cpp", "h", "hpp", "cs", "rb", "php", "swift", "kt",
            "scala", "sql", "sh", "bat", "ps1", "yaml", "yml", "xml",
            "json", "html", "css", "scss", "less", "md", "txt", "gradle",
            "properties", "toml", "cfg", "conf", "ini"
    );

    public AiProvider route(SubmissionFile file) {
        String fileName = file.getFileName();
        if (fileName == null) return deepSeekProvider;

        String ext = getExtension(fileName).toLowerCase();
        if (IMAGE_EXTENSIONS.contains(ext)) {
            return qwenVisionProvider;
        }
        return deepSeekProvider;
    }

    public boolean hasImageFiles(List<SubmissionFile> files) {
        return files.stream().anyMatch(f -> {
            String ext = getExtension(f.getFileName()).toLowerCase();
            return IMAGE_EXTENSIONS.contains(ext);
        });
    }

    public boolean hasCodeFiles(List<SubmissionFile> files) {
        return files.stream().anyMatch(f -> {
            String ext = getExtension(f.getFileName()).toLowerCase();
            return CODE_EXTENSIONS.contains(ext);
        });
    }

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot + 1) : "";
    }
}

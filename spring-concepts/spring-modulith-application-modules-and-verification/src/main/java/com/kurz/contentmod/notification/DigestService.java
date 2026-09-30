package com.kurz.contentmod.notification;

import java.util.List;

import com.kurz.contentmod.publishing.Content;
import com.kurz.contentmod.publishing.ContentRepository;
import org.springframework.stereotype.Service;

/** Somebody needed "recent content" and simply autowired the other module's repository. */
@Service
public class DigestService {

    private final ContentRepository contents;

    DigestService(ContentRepository contents) {
        this.contents = contents;
    }

    public String weeklyDigest() {
        List<Content> all = contents.findAll();
        return "This week: " + String.join(", ", all.subList(Math.max(0, all.size() - 3), all.size())
                .reversed().stream().map(Content::title).toList());
    }
}

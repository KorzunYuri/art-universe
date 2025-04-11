package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service;

import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;

import java.util.List;

public interface LastfmTagService {
    List<LastfmTag> findAllByNameIn(List<String> tagNames);
    List<LastfmTag> saveTags(List<LastfmTag> tags);
}

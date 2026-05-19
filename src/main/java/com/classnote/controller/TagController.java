package com.classnote.controller;

import com.classnote.common.ApiResponse;
import com.classnote.dto.TagRequest;
import com.classnote.dto.TagResponse;
import com.classnote.entity.Tag;
import com.classnote.service.TagService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    public ApiResponse<TagResponse> create(@Valid @RequestBody TagRequest req) {
        Tag tag = tagService.create(req);
        return ApiResponse.success("创建成功", TagResponse.from(tag));
    }

    @GetMapping
    public ApiResponse<List<TagResponse>> list(@RequestParam(defaultValue = "name,asc") String sort) {
        List<Tag> tags = tagService.list(sort);
        List<TagResponse> enriched = tags.stream().map(t -> {
            TagResponse r = TagResponse.from(t);
            r.setNoteCount(tagService.countNotesByTag(t.getId()));
            return r;
        }).collect(Collectors.toList());
        return ApiResponse.success(enriched);
    }

    @PutMapping("/{id}")
    public ApiResponse<TagResponse> update(@PathVariable String id, @Valid @RequestBody TagRequest req) {
        return ApiResponse.success(TagResponse.from(tagService.update(id, req)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable String id) {
        long count = tagService.delete(id);
        return ApiResponse.success("删除成功，已解除与 " + count + " 篇笔记的关联", null);
    }
}

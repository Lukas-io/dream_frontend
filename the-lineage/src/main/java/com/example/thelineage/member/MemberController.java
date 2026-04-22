package com.example.thelineage.member;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService service;

    public MemberController(MemberService service) {
        this.service = service;
    }

    @GetMapping
    public List<Member> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Member get(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/{id}/children")
    public List<Member> children(@PathVariable Long id) {
        return service.findChildren(id);
    }

    @PostMapping
    public ResponseEntity<Member> create(@Valid @RequestBody Member member) {
        Member created = service.create(member);
        return ResponseEntity.created(URI.create("/api/members/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public Member update(@PathVariable Long id, @Valid @RequestBody Member member) {
        return service.update(id, member);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.example.thelineage.member;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository repository;

    public MemberService(MemberRepository repository) {
        this.repository = repository;
    }

    public List<Member> findAll() {
        return repository.findAll();
    }

    public Member findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));
    }

    public List<Member> findChildren(Long parentId) {
        return repository.findByParentId(parentId);
    }

    public Member create(Member member) {
        member.setId(null);
        return repository.save(member);
    }

    public Member update(Long id, Member updated) {
        Member existing = findById(id);
        existing.setName(updated.getName());
        existing.setBirthYear(updated.getBirthYear());
        existing.setDeathYear(updated.getDeathYear());
        existing.setParent(updated.getParent());
        return repository.save(existing);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new MemberNotFoundException(id);
        }
        repository.deleteById(id);
    }
}

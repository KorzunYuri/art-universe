package yurykorzun.art.universe.music.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.quiz.entity.Step;

@Repository
public interface StepRepository extends JpaRepository<Step, Long> {
}

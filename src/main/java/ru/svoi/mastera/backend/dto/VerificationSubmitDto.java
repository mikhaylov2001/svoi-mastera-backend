package ru.svoi.mastera.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationSubmitDto {
    /** Индекс выбранного варианта по каждому вопросу теста (порядок как на фронте). */
    private List<Integer> quizAnswers;
    private VerificationSignaturePayloadDto signature;
}

package org.wainodu.mythirdtestappspringboot.service;

import org.springframework.stereotype.Service;
import org.wainodu.mythirdtestappspringboot.exception.UnsupportedCodeException;
@Service
public class UnsupportedCodeValidationService {
    public void isValid(int uid) throws UnsupportedCodeException {
        if (uid == 123) {
            throw new UnsupportedCodeException();
        }
    }
}

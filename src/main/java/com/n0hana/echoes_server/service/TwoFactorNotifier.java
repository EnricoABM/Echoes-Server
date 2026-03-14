package com.n0hana.echoes_server.service;

import com.n0hana.echoes_server.dto.TwoFactorDto;

public interface TwoFactorNotifier {
  void send(TwoFactorDto dto);
}

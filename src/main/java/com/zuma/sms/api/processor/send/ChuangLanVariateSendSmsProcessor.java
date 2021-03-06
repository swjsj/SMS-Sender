package com.zuma.sms.api.processor.send;

import com.zuma.sms.config.ConfigStore;
import com.zuma.sms.dto.api.ChuangLanAPI;
import com.zuma.sms.entity.Channel;
import com.zuma.sms.enums.error.ChuangLanErrorEnum;
import com.zuma.sms.enums.system.ErrorEnum;
import com.zuma.sms.exception.SmsSenderException;
import com.zuma.sms.util.CodeUtil;
import com.zuma.sms.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * author:ZhengXing
 * datetime:2017/12/15 0015 16:18
 * 创蓝 短信发送
 */
@Component
@Slf4j
public class ChuangLanVariateSendSmsProcessor extends AbstractSendSmsProcessor<ChuangLanAPI.VariateRequest,ChuangLanAPI.VariateResponse,ChuangLanErrorEnum>{

	@Autowired
	private HttpClientUtil httpClientUtil;

	@Autowired
	private ConfigStore configStore;



	@Override
	protected ChuangLanAPI.VariateRequest toRequestObject(Channel channel, String phones, String message) {

		return new ChuangLanAPI.VariateRequest(channel.getAKey(), channel.getBKey(), message, phones);
	}



	@Override
	protected UpdateRecordInfo<ChuangLanErrorEnum> getUpdateRecordInfo(ChuangLanAPI.VariateResponse response) {
		return new UpdateRecordInfo<>(response.getMsgId(),response.getCode(),
				ChuangLanErrorEnum.class,ChuangLanErrorEnum.SUCCESS);
	}

	@Override
	protected String send(ChuangLanAPI.VariateRequest requestObject) {
		try {
			return httpClientUtil.doPostForString(configStore.chuanglanVariateSendSmsUrl, CodeUtil.objectToJsonString(requestObject));
		} catch (Exception e) {
			log.error("[短信发送过程]短信发送http失败.e:{}",e.getMessage(),e);
			throw new SmsSenderException(ErrorEnum.HTTP_ERROR);
		}
	}

	@Override
	ChuangLanAPI.VariateResponse stringToResponseObject(String result) {
		try {
			return CodeUtil.jsonStringToObject(result,ChuangLanAPI.VariateResponse.class);
		} catch (Exception e) {
			log.error("[短信发送过程]返回的string转为response对象失败.resultString={},error={}", result, e.getMessage(), e);
			throw new SmsSenderException(ErrorEnum.STRING_TO_RESPONSE_ERROR);
		}
	}
}

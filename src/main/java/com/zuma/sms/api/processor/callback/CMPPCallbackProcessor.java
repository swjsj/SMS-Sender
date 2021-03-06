package com.zuma.sms.api.processor.callback;

import com.zuma.sms.dto.SendData;
import com.zuma.sms.dto.ResultDTO;
import com.zuma.sms.dto.api.cmpp.CMPPSubmitAPI;
import com.zuma.sms.entity.Channel;
import com.zuma.sms.entity.SmsSendRecord;
import com.zuma.sms.enums.ResultDTOTypeEnum;
import com.zuma.sms.enums.error.CMPPSubmitErrorEnum;
import com.zuma.sms.util.EnumUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * author:ZhengXing
 * datetime:2017/12/18 0018 17:20
 * cmpp 同步回调(算是) 短信发送过程
 */
@Component
@Slf4j
public class CMPPCallbackProcessor extends SendSmsCallbackProcessor<CMPPSubmitAPI.Response> {


	/**
	 * 重写通用处理部分
	 */
	@Override
	protected boolean commonProcess(SmsSendRecord record, ResultDTO<SendData> resultDTO, Channel channel) {
		//如果成功
		if (ResultDTO.isSuccess(resultDTO))
			return true;
		//失败处理
		//如果是定时任务
		if(record.isTaskRecord())
			taskHandle(resultDTO,record,channel);
		else
			//如果是其他平台,直接通知其已经失败
			sendCallback(resultDTO,record.getPlatformId());
		return true;
	}

	/**
	 * 该类需要重写该方法
	 * @param resultDTO 返回对象
	 * @param record    记录
	 * @param channel   短信通道
	 */
	@Override
	protected void taskHandle(ResultDTO<SendData> resultDTO, SmsSendRecord record, Channel channel) {
		//该处理类,成功时,不累加,因为之前全都直接返回成功了,失败,才累加
		if(!ResultDTO.isSuccess(resultDTO))
			sendTaskManager.asyncStatusIncrement(record.getSendTaskId(),false,resultDTO.getData().getCount());
	}

	@Override
	protected String getOtherId(CMPPSubmitAPI.Response response) {
		return String.valueOf(response.getSequenceId());
	}

	@Override
	protected ResultDTO<SendData> getResultDTO(CMPPSubmitAPI.Response response, SmsSendRecord record) {
		//如果成功
		if(CMPPSubmitErrorEnum.SUCCESS.getCode().equals((int)response.getResult())){
			return ResultDTO.success(new SendData()).setType(ResultDTOTypeEnum.SEND_SMS_CALLBACK_SYNC.getCode());
		}
		//失败
		//找到失败码对应枚举
		CMPPSubmitErrorEnum errorEnum = EnumUtil.getByCode((int) response.getResult(), CMPPSubmitErrorEnum.class);
		//返回失败信息
		return ResultDTO.errorOfInteger(errorEnum,new SendData(getPhoneLen(record.getPhones()),record.getPhones(),record.getMessage()));
	}
}

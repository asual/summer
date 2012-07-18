/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.asual.summer.sample.web;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.octo.captcha.service.image.ImageCaptchaService;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Controller
public class CaptchaController {
	
	@Inject
	private ImageCaptchaService captchaService;
	
	@RequestMapping("/captcha")
	public void handleRequest(HttpServletRequest req, HttpServletResponse response,
			@RequestParam(value="hash", required=true) String hash) throws IOException {

		response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/png");

    	BufferedImage challenge = captchaService.getImageChallengeForID(hash, req.getLocale());
        ImageIO.write(challenge, "png", response.getOutputStream());
    	
        response.getOutputStream().flush();
        response.getOutputStream().close();
	}

}
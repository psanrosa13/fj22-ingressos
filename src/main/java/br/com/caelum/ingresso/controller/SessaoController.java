package br.com.caelum.ingresso.controller;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.sun.mail.handlers.image_gif;

import br.com.caelum.ingresso.business.GerenciadorDeSessao;
import br.com.caelum.ingresso.dao.FilmeDao;
import br.com.caelum.ingresso.dao.SalaDao;
import br.com.caelum.ingresso.dao.SessaoDao;
import br.com.caelum.ingresso.model.ImagemCapa;
import br.com.caelum.ingresso.model.Sessao;
import br.com.caelum.ingresso.model.TipoDeIngresso;
import br.com.caelum.ingresso.model.form.SessaoForm;
import br.com.caelum.ingresso.rest.ImdbClient;

@Controller
public class SessaoController {
	
	@Autowired
	private SalaDao salaDao;
	
	@Autowired
	private FilmeDao filmeDao;
	
	@Autowired
	private SessaoDao sessaoDao;
	
	@Autowired
	private ImdbClient client;
	
	@GetMapping("/admin/sessao")
	public ModelAndView form(@RequestParam("salaId") Integer idSala, SessaoForm form){
		
		form.setSalaId(idSala);
		
		ModelAndView mav = new ModelAndView("sessao/sessao");
		
		mav.addObject("sala", salaDao.findOne(idSala) );
		mav.addObject("filmes", filmeDao.findAll());
		mav.addObject("form", form);
		
		return mav;
	}

	@PostMapping("admin/sessao")
	@Transactional
	public ModelAndView salva(@Valid SessaoForm form, BindingResult result){
		if(result.hasErrors())return form(form.getSalaId(), form);
		
		Sessao sessao = form.toSessao(salaDao, filmeDao);
		
		GerenciadorDeSessao gerenciador = new GerenciadorDeSessao(sessaoDao.buscaSessoesDaSala(sessao.getSala()));
		
		if(gerenciador.cabe(sessao)){
			sessaoDao.save(sessao);
			return new ModelAndView("redirect:/admin/sala/"+form.getSalaId()+"/sessoes");
		}
		return 	form(form.getSalaId(),	form);
	}
	
	@DeleteMapping("/admin/sessao/{id}")
	@ResponseBody
	@Transactional
	public void delete(@PathVariable("id") Integer id){
	    sessaoDao.delete(id);
	}
	
	@GetMapping("/sessao/{id}/lugares")
	public ModelAndView lugaresNaSessao(@PathVariable("id") Integer sessaoId){
		ModelAndView modelAndView = new ModelAndView("sessao/lugares");
		
		Sessao sessao = sessaoDao.findOne(sessaoId);
		Optional<ImagemCapa> imagemCapa = client.request(sessao.getFilme(), ImagemCapa.class);
		
		modelAndView.addObject("sessao",sessao);
		modelAndView.addObject("imagemCapa", imagemCapa.orElse(new ImagemCapa()));
		modelAndView.addObject("tiposDeIngressos", TipoDeIngresso.values());
		
		return modelAndView;
	}
	
}

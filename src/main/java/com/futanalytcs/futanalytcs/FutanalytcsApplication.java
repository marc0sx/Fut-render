package com.futanalytcs.futanalytcs;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@SpringBootApplication
public class FutanalytcsApplication {

	public static void main(String[] args) {
		SpringApplication.run(FutanalytcsApplication.class, args);
	}

	@Controller
	class AnalytControllerFut {
		private static final Logger logger = LoggerFactory.getLogger(AnalytControllerFut.class);

		@RequestMapping("/index")
		public String index() {
			return "index";
		}

		@PostMapping("/analyze")
		public String analyze(@RequestParam("url") String url, Model model) {
			try {
				Document document = Jsoup.connect(url).get();

				String nomeTime1 = "";
				String nomeTime2 = "";

				Elements nomeCasa = document.select(".stats-game-head-teamname.hide-mobile a");
				Elements nomeFora = document.select(".stats-subtitle");

				if (nomeCasa.size() >= 2) {
					nomeTime1 = nomeCasa.get(1).text();
					nomeTime2 = nomeFora.get(1).text();
				} else {
					model.addAttribute("resultado", "Não foi possível obter os nomes dos times.");
					logger.warn("Não foi possível obter os nomes dos times.");
					return "index";
				}

				model.addAttribute("times1", nomeTime1);
				model.addAttribute("times2", nomeTime2);
				model.addAttribute("odds", "TABELA DE ODDS");
				model.addAttribute("vic", "Vitória");
				model.addAttribute("emp", "Empate");
				model.addAttribute("der", "Derrota");


				Elements oddsElements = document.select(".stats-group.odds.full.odds_MO");

				if (!oddsElements.isEmpty()) {
					Element oddsElement = oddsElements.first();
					Elements tbodyElements = oddsElement.select("tbody");
					int numColumns = tbodyElements.first().select("tr").first().select("td").size();

					double[] maxValues = processOdds(tbodyElements.select("tr"), numColumns);

					model.addAttribute("maioresodds", maxValues);

				} else {
					logger.warn("Nenhuma ocorrência da classe 'stats-group odds full odds_MO'.");
				}

				// Chamar o método para obter estatísticas de gols
				processGoalStatistics(document, model);

				// Process statistics for the away team
				processAwayTeamStatistics(document, model);

				// Chamar o método para obter estatísticas dos últimos 10 jogos
				DezUltimos(document, model);

				// Chamar o método para obter estatísticas dos últimos 10 jogos fora
				DezUltimosFora(document, model);


				calculateGoalsAverage(document, model);
				SomaDasMediasTodosJogos(document, model);


				// Configurar a variável para exibir o conteúdo
				model.addAttribute("exibirConteudo", true);

			} catch (IOException e) {
				logger.error("Erro ao analisar a URL.", e);
				model.addAttribute("resultado", "Erro ao analisar a URL.");

				// Configurar a variável para não exibir o conteúdo em caso de erro
				model.addAttribute("exibirConteudo", false);
			}

			return "index";
		}

		private void processGoalStatistics(Document document, Model model) {
			Elements statElements = document.select(".stat-last10.stat-half-padding tbody");

			if (statElements.size() >= 3) {
				Element thirdStatElement = statElements.get(2);
				Elements tbodyElements = thirdStatElement.select("tbody");

				for (Element tbody : tbodyElements) {
					Elements rows = tbody.select("tr");

					int vitoriasCasa = 0;
					int derrotasCasa = 0;
					int empates = 0;
					int totalGolsCasa = 0;

					for (Element row : rows) {
						Elements cells = row.select("td");

						for (int i = 0; i < cells.size(); i++) {
							if (i != 0 && i != 4) {

							}
						}

						String placar = cells.get(2).text();
						int golsCasa = Integer.parseInt(placar.split("-")[0]);
						int golsFora = Integer.parseInt(placar.split("-")[1]);

						if (golsCasa > golsFora) {
							vitoriasCasa++;
						} else if (golsCasa < golsFora) {
							derrotasCasa++;
						} else {
							empates++;
						}

						totalGolsCasa += golsCasa;

					}

					calcularProbabilidades(model, vitoriasCasa, derrotasCasa, empates, rows.size());
					calcularMediaGols(model, totalGolsCasa, rows.size(), "Casa");


				}
			} else {
				logger.warn("Menos de três ocorrências da classe 'stat-last10.stat-half-padding'.");
			}
		}

		private void processAwayTeamStatistics(Document document, Model model) {
			Elements statElementsFora = document.select(".stat-last10.stat-half-padding");

			if (statElementsFora.size() >= 4) {
				Element fourthStatElement = statElementsFora.get(3);
				Elements tbodyElements = fourthStatElement.select("tbody");

				for (Element tbody : tbodyElements) {
					Elements rows = tbody.select("tr");

					int vitoriasFora = 0;
					int derrotasFora = 0;
					int empatesFora = 0;
					int totalGolsFora = 0;

					for (Element row : rows) {
						Elements cells = row.select("td");

						for (int i = 0; i < cells.size(); i++) {
							if (i != 0 && i != 4) {
								// Exibir as informações de cada célula
								// System.out.print(cells.get(i).text() + "\t");
							}
						}

						String placar = cells.get(2).text();
						int golsCasa = Integer.parseInt(placar.split("-")[0]);
						int golsFora = Integer.parseInt(placar.split("-")[1]);

						if (golsCasa < golsFora) {
							vitoriasFora++;
						} else if (golsCasa > golsFora) {
							derrotasFora++;
						} else {
							empatesFora++;
						}

						totalGolsFora += golsFora;

						// System.out.println(); // Nova linha para separar as linhas
					}

					calcularProbabilidades(model, vitoriasFora, derrotasFora, empatesFora, rows.size(), "Fora");
					calcularMediaGols(model, totalGolsFora, rows.size(), "Fora");


				}
			} else {
				logger.warn(
						"Menos de quatro ocorrências da classe 'stat-last10.stat-half-padding' para o time de fora.");
			}
		}







		private void DezUltimos(Document document, Model model) {
			Elements statElements = document.select(".stat-last10.stat-half-padding tbody");

			if (statElements.size() >= 1) {
				Element firstStatElement = statElements.get(0);
				Elements tbodyElements = firstStatElement.select("tbody");

				String nomeTime1 = "";
				Elements nomeCasa = document.select(".stats-game-head-teamname.hide-mobile a");

				if (nomeCasa.size() >= 2) {
					nomeTime1 = nomeCasa.get(1).text();
				}

				double mediaGolsTime1 = 0.0; // Alteração para double
				int totalGolsTime1 = 0;
				int vitoriasTime1 = 0;
				int empatesTime1 = 0;
				int derrotasTime1 = 0;
				int linhasProcessadas = 0; // Adicionado contador para limitar o processamento a 10 linhas
				int totalJogos = 10;

				for (Element tbody : tbodyElements) {
					Elements rows = tbody.getElementsByTag("tr");

					if (rows != null && !rows.isEmpty()) {
						for (Element row : rows) {
							Elements cells = row.getElementsByTag("td");

							if (cells.size() >= 5) {
								String timeCasa = cells.get(2).text();
								String placar = cells.get(3).text();
								String timeFora = cells.get(4).text();

								try {
									int golsCasa = Integer.parseInt(placar.substring(0, 1));
									int golsFora = Integer.parseInt(placar.substring(2, 3));

									if (nomeTime1.toLowerCase().contains(timeCasa.toLowerCase())) {
										totalGolsTime1 += golsCasa;

										if (golsCasa > golsFora) {
											vitoriasTime1++;
										} else if (golsCasa < golsFora) {
											derrotasTime1++;
										} else {
											empatesTime1++;
										}
									} else if (nomeTime1.toLowerCase().contains(timeFora.toLowerCase())) {
										totalGolsTime1 += golsFora;

										if (golsFora > golsCasa) {
											vitoriasTime1++;
										} else if (golsFora < golsCasa) {
											derrotasTime1++;
										} else {
											empatesTime1++;
										}
									}

									linhasProcessadas++;

									if (linhasProcessadas >= 10) {
										// Parar o processamento após 10 linhas
										break;
									}

								} catch (NumberFormatException e) {
									// Trate a exceção, por exemplo, atribuindo um valor padrão ou registrando um aviso
									logger.warn("Erro ao converter placar para númeross: " + e.getMessage());
								}
							}
						}

						// Adicionar informações ao modelo para exibição no front-end
						if (!rows.isEmpty()) {
							double percentualVitoriasCasa = (double) vitoriasTime1 / totalJogos * 100;
							double percentualDerrotasCasa = (double) derrotasTime1 / totalJogos * 100;
							double percentualEmpatesCasa = (double) empatesTime1 / totalJogos * 100;



							// Porcentagem estatisticas_casa
							model.addAttribute("vitoriasCasaPorcentagem", String.format("%.2f", percentualVitoriasCasa) + "%");
							model.addAttribute("derrotasCasaPorcentagem", String.format("%.2f", percentualDerrotasCasa) + "%");
							model.addAttribute("empatesCasaPorcentagem", String.format("%.2f", percentualEmpatesCasa) + "%");

							mediaGolsTime1 = (double) totalGolsTime1 / 10; // Atualização para double

							model.addAttribute("mediaGolsTime1",
									"Média de Gols por Jogo " + ": " + String.format("%.2f", mediaGolsTime1).replace(",", "."));
							model.addAttribute("vitoriasTime1", vitoriasTime1);
							model.addAttribute("empatesTime1", empatesTime1);
							model.addAttribute("derrotasTime1", derrotasTime1);


						}

					} else {
						logger.warn("Nenhuma ocorrência de linhas para 'tr' nos elementos 'tbody'.");
					}
				}
			} else {
				logger.warn("Menos de uma ocorrência da classe 'stat-last10.stat-half-padding'.");
			}
		}


		private void DezUltimosFora(Document document, Model model) {
			Elements statElementsFora = document.select(".stat-last10.stat-half-padding");

			if (statElementsFora.size() >= 2) {
				Element secondStatElement = statElementsFora.get(1); // Alterado para obter a segunda ocorrência (índice 1)
				Elements tbodyElements = secondStatElement.select("tbody"); // Removido ".first()"

				if (tbodyElements != null && !tbodyElements.isEmpty()) { // Verifica se tbodyElements é não nulo e não vazio
					String nomeTime2 = "";
					Elements nomeFora = document.select(".stats-subtitle");

					if (nomeFora.size() >= 3) {
						nomeTime2 = nomeFora.get(1).text();
					}
					double mediaGolsTime2 = 0.0;
					int totalGolsTime2 = 0;
					int vitoriasTime2 = 0;
					int empatesTime2 = 0;
					int derrotasTime2 = 0;
					int linhasProcessadas2 = 0; // Adicionado contador para limitar o processamento a 10 linhas
					int totalJogos = 10;

					for (Element tbody : tbodyElements) {
						Elements rows = tbody.getElementsByTag("tr");

						for (int i = 0; i < Math.min(10, rows.size()); i++) {
							Element row = rows.get(i);
							Elements cells = row.getElementsByTag("td");

							if (cells.size() >= 5) {
								String timeCasa = cells.get(2).text();
								String placar = cells.get(3).text();
								String timeFora = cells.get(4).text();

								try {
									int golsCasa = Integer.parseInt(placar.substring(0, 1));
									int golsFora = Integer.parseInt(placar.substring(2, 3));

									if (nomeTime2.toLowerCase().contains(timeCasa.toLowerCase())) {
										//(nomeTime2.toLowerCase().contains(timeCasa.toLowerCase()))


										totalGolsTime2 += golsCasa;

										if (golsCasa > golsFora) {
											vitoriasTime2++;
										} else if (golsCasa < golsFora) {
											derrotasTime2++;
										} else {
											empatesTime2++;
										}
									} else if (nomeTime2.toLowerCase().contains(timeFora.toLowerCase())) {
										//(nomeTime2.toLowerCase().contains(timeFora.toLowerCase()))


										totalGolsTime2 += golsFora;

										if (golsFora > golsCasa) {
											vitoriasTime2++;
										} else if (golsFora < golsCasa) {
											derrotasTime2++;
										} else {
											empatesTime2++;
										}
									}

									linhasProcessadas2++;

									if (linhasProcessadas2 >= 10) {
										// Parar o processamento após 10 linhas
										break;
									}

								} catch (NumberFormatException e) {
									// Trate a exceção, por exemplo, atribuindo um valor padrão ou registrando um
									// aviso
									logger.warn("Erro ao converter placar para númeross: " + e.getMessage());
								}
							}
						}

						// Adicionar informações ao modelo para exibição no front-end
						if (!rows.isEmpty()) {

							double percentualVitoriasCasa = (double) vitoriasTime2 / totalJogos * 100;
							double percentualDerrotasCasa = (double) derrotasTime2 / totalJogos * 100;
							double percentualEmpatesCasa = (double) empatesTime2 / totalJogos * 100;
							// Porcentagem estatisticas_casa
							model.addAttribute("vitoriasForaPorcentagem", String.format("%.2f", percentualVitoriasCasa) + "%");
							model.addAttribute("derrotasForaPorcentagem", String.format("%.2f", percentualDerrotasCasa) + "%");
							model.addAttribute("empatesForaPorcentagem", String.format("%.2f", percentualEmpatesCasa) + "%");

							mediaGolsTime2 = (double) totalGolsTime2 / 10; // Atualização para double

							model.addAttribute("mediaGolsTime2",
									"Média de Gols por Jogo " + ": " + String.format("%.2f", mediaGolsTime2).replace(",", "."));
							model.addAttribute("vitoriasTime2", vitoriasTime2);
							model.addAttribute("empatesTime2", empatesTime2);
							model.addAttribute("derrotasTime2", derrotasTime2);


						}
					}
				} else {
					logger.warn("Nenhum elemento 'tbody' encontrado dentro do segundo elemento 'stat-last10.stat-half-padding'.");
				}
			} else {
				logger.warn("Menos de duas ocorrências da classse 'stat-last10.stat-half-padding'.");
			}
		}



		private void calculateGoalsAverage(Document document, Model model) {
			Elements statElements = document.select(".stat-last10.stat-half-padding tbody");
			double mediaGolsTime1 = 0.0;
			double mediaGolsTime2 = 0.0;
			int totalGolsTime1 = 0;
			int totalGolsTime2 = 0;
			int vitoriasTime1 = 0;
			int empatesTime1 = 0;
			int derrotasTime1 = 0;
			int vitoriasTime2 = 0;
			int empatesTime2 = 0;
			int derrotasTime2 = 0;
			int totalJogos = 10;

			if (statElements.size() >= 1) {
				Element firstStatElement = statElements.get(0);
				Element secondStatElement = statElements.get(1);
				Elements tbodyElements1 = firstStatElement.select("tbody");
				Elements tbodyElements2 = secondStatElement.select("tbody");

				String nomeTime1 = "";
				Elements nomeCasa = document.select(".stats-game-head-teamname.hide-mobile a");

				if (nomeCasa.size() >= 2) {
					nomeTime1 = nomeCasa.get(1).text();
				}

				for (Element tbody1 : tbodyElements1) {
					Elements rows1 = tbody1.getElementsByTag("tr");

					if (rows1 != null && !rows1.isEmpty()) {
						for (Element row1 : rows1) {
							Elements cells1 = row1.getElementsByTag("td");

							if (cells1.size() >= 5) {
								String timeCasa = cells1.get(2).text();
								String placar = cells1.get(3).text();
								String timeFora = cells1.get(4).text();

								try {
									int golsCasa = Integer.parseInt(placar.substring(0, 1));
									int golsFora = Integer.parseInt(placar.substring(2, 3));

									if (nomeTime1.toLowerCase().contains(timeCasa.toLowerCase())) {
										totalGolsTime1 += golsCasa;

										if (golsCasa > golsFora) {
											vitoriasTime1++;
										} else if (golsCasa < golsFora) {
											derrotasTime1++;
										} else {
											empatesTime1++;
										}
									} else if (nomeTime1.toLowerCase().contains(timeFora.toLowerCase())) {
										totalGolsTime1 += golsFora;

										if (golsFora > golsCasa) {
											vitoriasTime1++;
										} else if (golsFora < golsCasa) {
											derrotasTime1++;
										} else {
											empatesTime1++;
										}
									}
								} catch (NumberFormatException e) {
									logger.warn("Erro ao converter placar para númeross: " + e.getMessage());
								}
							}
						}
					}
				}

				mediaGolsTime1 = (double) totalGolsTime1 / totalJogos;

				String nomeTime2 = "";
				Elements nomeFora = document.select(".stats-subtitle");

				if (nomeFora.size() >= 3) {
					nomeTime2 = nomeFora.get(1).text();
				}

				for (Element tbody2 : tbodyElements2) {
					Elements rows2 = tbody2.getElementsByTag("tr");

					if (rows2 != null && !rows2.isEmpty()) {
						for (Element row2 : rows2) {
							Elements cells2 = row2.getElementsByTag("td");

							if (cells2.size() >= 5) {
								String timeCasa = cells2.get(2).text();
								String placar = cells2.get(3).text();
								String timeFora = cells2.get(4).text();

								try {
									int golsCasa = Integer.parseInt(placar.substring(0, 1));
									int golsFora = Integer.parseInt(placar.substring(2, 3));

									if (nomeTime2.toLowerCase().contains(timeCasa.toLowerCase())) {
										totalGolsTime2 += golsCasa;

										if (golsCasa > golsFora) {
											vitoriasTime2++;
										} else if (golsCasa < golsFora) {
											derrotasTime2++;
										} else {
											empatesTime2++;
										}
									} else if (nomeTime2.toLowerCase().contains(timeFora.toLowerCase())) {
										totalGolsTime2 += golsFora;

										if (golsFora > golsCasa) {
											vitoriasTime2++;
										} else if (golsFora < golsCasa) {
											derrotasTime2++;
										} else {
											empatesTime2++;
										}
									}
								} catch (NumberFormatException e) {
									logger.warn("Erro ao converter placar para númeross: " + e.getMessage());
								}
							}
						}
					}
				}

				mediaGolsTime2 = (double) totalGolsTime2 / totalJogos;
				double SomaMedias10ultimos = (double) mediaGolsTime1 + mediaGolsTime2;

				model.addAttribute("somaMedias", String.format("%.2f", SomaMedias10ultimos).replace(",", "."));
			}}




		private void SomaDasMediasTodosJogos(Document document, Model model) {
			String nomeTime1 = "";
			String nomeTime2 = "";

			Elements nomeCasa = document.select(".stats-game-head-teamname.hide-mobile a");
			Elements nomeFora = document.select(".stats-subtitle");

			if (nomeCasa.size() >= 2) {
				nomeTime1 = nomeCasa.get(1).text();
				nomeTime2 = nomeFora.get(1).text();
			} else {
				model.addAttribute("resultado", "Não foi possível obter os nomes dos times.");
				logger.warn("Não foi possível obter os nomes dos times.");

			}

			// ---------------- MEDIA DO TIME DA CASA ----------------

			Elements statElements = document.select(".stat-last10.stat-half-padding tbody");

			double totalMediaGolsCasa = 0;

			// Check if there are at least three occurrences
			if (statElements.size() >= 3) {
				// Get the third element
				Element thirdStatElement = statElements.get(2);

				// In the loop, no need to print individual cell values
				Elements tbodyElements = thirdStatElement.select("tbody");

				for (Element tbody : tbodyElements) {
					Elements rows = tbody.select("tr");

					int totalGolsCasa = 0;

					for (Element row : rows) {
						Elements cells = row.select("td");

						try {
							String placar = cells.get(2).text();
							int golsCasa = Integer.parseInt(placar.split("-")[0]);

							totalGolsCasa += golsCasa;
						} catch (Exception e) {
							logger.error("Time da casa teve 1 ou + jogos adiados: " + e.getMessage());
							model.addAttribute("alerta", "Um dos times teve um ou mais jogos adiados, podendo afetar as estatisticas!");
						}
					}

					// Calculate and accumulate the total media for Casa
					double mediaGolsCasa = calcularMediaGols(totalGolsCasa, rows.size(), "Casa");
					totalMediaGolsCasa += mediaGolsCasa;
				}
			} else {
				logger.warn("Menos de três ocorrências da classe 'stat-last10 stat-half-padding'.");
			}

			// ---------------- MEDIA DO TIME DE FORA ----------------

			Elements statElementsFora = document.select(".stat-last10.stat-half-padding");

			double totalMediaGolsFora = 0;

			if (statElementsFora.size() >= 4) {
				Element fourthStatElement = statElementsFora.get(3);

				Elements tbodyElements = fourthStatElement.select("tbody");

				for (Element tbody : tbodyElements) {
					Elements rows = tbody.select("tr");

					int totalGolsFora = 0;

					for (Element row : rows) {
						Elements cells = row.select("td");

						try {
							String placar = cells.get(2).text();
							int golsFora = Integer.parseInt(placar.split("-")[1]);

							totalGolsFora += golsFora;
						} catch (Exception e) {
							logger.error("Time de fora teve 1 ou + jogos adiados: " + e.getMessage());
							model.addAttribute("alerta", "Um dos times teve um ou mais jogos adiados, podendo afetar as estatisticas!");
						}
					}

					// Calculate and accumulate the total media for Fora
					double mediaGolsFora = calcularMediaGols(totalGolsFora, rows.size(), "Fora");
					totalMediaGolsFora += mediaGolsFora;
				}
			} else {
				logger.warn("Menos de quatro ocorrências da classe 'stat-last10 stat-half-padding'.");
			}


			// Defina o resultado no modelo a ser exibido no HTML
			model.addAttribute("times", (nomeTime1 + "  x  " + nomeTime2));

			// Add the calculated averages to the model attributes
			/*
			model.addAttribute("casa", String.format("Média de Gols por Jogo Casa: %.2f", totalMediaGolsCasa));
			model.addAttribute("fora", String.format("Média de Gols por Jogo Fora: %.2f", totalMediaGolsFora));
			*/

			model.addAttribute("somaMediasTodosJogos", String.format("%.2f", (totalMediaGolsCasa + totalMediaGolsFora)).replace(",", "."));


		}





		private void calcularProbabilidades(Model model, int vitorias, int derrotas, int empates, int totalJogos) {
			double percentualVitorias = (double) vitorias / totalJogos * 100;
			double percentualDerrotas = (double) derrotas / totalJogos * 100;
			double percentualEmpates = (double) empates / totalJogos * 100;
			// Porcentagem estatisticas_casa
			model.addAttribute("vitoriasCasa", String.format("%.2f", percentualVitorias) + "%");
			model.addAttribute("derrotasCasa", String.format("%.2f", percentualDerrotas) + "%");
			model.addAttribute("empatesCasa", String.format("%.2f", percentualEmpates) + "%");

			// Quantidade casa
			model.addAttribute("quantidadeVitoriaCasa",vitorias);
			model.addAttribute("quantidadeDerrotaCasa",derrotas);
			model.addAttribute("quantidadeEmpateCasa", empates);

		}

		private void calcularProbabilidades(Model model, int vitorias, int derrotas, int empates, int totalJogos,
											String local) {
			double percentualVitorias = (double) vitorias / totalJogos * 100;
			double percentualDerrotas = (double) derrotas / totalJogos * 100;
			double percentualEmpates = (double) empates / totalJogos * 100;

			// Porcentagem estatisticas_casa
			model.addAttribute("vitoriasFora", String.format("%.2f", percentualVitorias) + "%");
			model.addAttribute("derrotasFora", String.format("%.2f", percentualDerrotas) + "%");
			model.addAttribute("empatesFora", String.format("%.2f", percentualEmpates) + "%");

			// Quantidade casa
			model.addAttribute("quantidadeVitoriaFora",vitorias);
			model.addAttribute("quantidadeDerrotaFora",derrotas);
			model.addAttribute("quantidadeEmpateFora", empates);
		}
		/*10 ultimos jogos*/
		private void calcularMediaGols(Model model, int totalGols, int totalJogos, String local) {
			double mediaGols = (double) totalGols / totalJogos;
			model.addAttribute("mediaGols" + local,
					"Média de Gols por Jogo " + local + ": " + String.format("%.2f", mediaGols).replace(",", "."));
		}
		/*Todos os jogos*/
		private double calcularMediaGols(int totalGols, int totalJogos, String local) {
			if (totalJogos == 0) {
				return 0;  // To avoid division by zero
			}
			double mediaGols = (double) totalGols / totalJogos;
			logger.info(String.format("Média de Gols por Jogo %s: %.2f", local, mediaGols));
			return mediaGols;
		}



		private double[] processOdds(Elements rows, int numColumns) {
			double[] maxValues = new double[numColumns];

			for (int i = 0; i < numColumns; i++) {
				maxValues[i] = Double.NEGATIVE_INFINITY;
			}

			for (int i = 0; i < rows.size(); i++) {
				if (!rows.get(i).text().isEmpty()) {
					Elements cells = rows.get(i).select("td");

					for (int j = 0; j < numColumns; j++) {
						if (!cells.get(j).text().isEmpty() && !"-Infinity".equals(cells.get(j).text())) {
							double cellValue = Double.parseDouble(cells.get(j).text());

							if (cellValue > maxValues[j]) {
								maxValues[j] = cellValue;
							}
						}
					}
				}
			}

			return maxValues;
		}
	}
}
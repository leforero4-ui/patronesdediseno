package main.infrastructure.driver.adapter.controller;

import main.application.driven.port.provider.Drawable;
import main.application.driver.adapter.usecase.Game;
import main.application.driver.adapter.usecase.factory_enemies.EnemyBasicMethod;
import main.application.driver.adapter.usecase.factory_enemies.EnemyHighMethod;
import main.application.driver.adapter.usecase.factory_enemies.EnemyMiddleMethod;
import main.application.driver.port.controller.Controller;
import main.application.driver.port.usecase.EnemyMethod;
import main.application.driver.port.usecase.GameableUseCase;
import main.domain.model.ArmyAirFactory;
import main.domain.model.ArmyFactory;
import main.domain.model.ArmyNavalFactory;
import main.domain.model.Player;
import main.domain.model.PlayerBuilder;
import main.infrastructure.driven.adapter.provider.LanternaDrawable;

public class ControllerImpl implements Controller {

	private GameableUseCase gameableUseCase;
	private final ArmyFactory armyFactory;
	private final Drawable drawable;
	private Player player;
	
	public ControllerImpl() {
		this.drawable = new LanternaDrawable();
		if("1".equalsIgnoreCase(this.drawable.in("naval oprima: 1\r\nair oprima: cualquier tecla"))) {
			this.drawable.out("naval:");
			this.armyFactory = new ArmyNavalFactory();
		} else {
			this.drawable.out("air:");
			this.armyFactory = new ArmyAirFactory();
		}
		this.setGameSetting();
	}
	
	private void setGameSetting() {
		final EnemyMethod enemyMethod = switch (this.drawable.in("nivel básico: 1\r\nnivel medio: 2\r\nnivel alto oprima: cualquier tecla")) {
			case "1" -> {
				this.drawable.out("básico:");
				yield new EnemyBasicMethod(armyFactory);
			}
			case "2" -> {
				this.drawable.out("medio:");
				yield new EnemyMiddleMethod(armyFactory);
			}
			default -> {
				this.drawable.out("alto:");
				yield new EnemyHighMethod(armyFactory);
			}
		};
		this.createPlayer();
		this.gameableUseCase = new Game(enemyMethod, this.player);
	}
	
	private void createPlayer() {
		this.drawable.out("crear jugador");
		final PlayerBuilder playerBuilder = new PlayerBuilder();
		playerBuilder.name(this.drawable.in("nombre:"));
		playerBuilder.typeEye(this.drawable.in("tipo de ojos:"));
		playerBuilder.typeHair(this.drawable.in("tipo de pelo:"));
		playerBuilder.typeShirt(this.drawable.in("tipo de camisa:"));
		playerBuilder.typePant(this.drawable.in("tipo de pantalón:"));
		playerBuilder.typeShoes(this.drawable.in("tipo de zapatos:"));
		this.player = this.armyFactory.createPlayer(playerBuilder);
	}
	
	@Override
	public void startGame() {
		this.gameableUseCase.startGame();
		String squares;
		
		String locationEnemy;
		do {
			squares = this.gameableUseCase.getStringAvatarSquares() + "\r\n";
			locationEnemy = this.drawable.in(squares + "elija fila y columna separado por guión(-) para atacar;\r\nescriba 'buscar:' seguido de los tipos de enemigos a buscar, ejemplo soldado y escuadron y (aire o naval)\r\n(99-99 para terminar juego)");
			if (locationEnemy != null && locationEnemy.contains("-") && !locationEnemy.equalsIgnoreCase("99-99")) {
				final String[] locationEnemySplit = locationEnemy.split("-");
				final int row = Integer.parseInt(locationEnemySplit[0]);
				final int column = Integer.parseInt(locationEnemySplit[1]);
				if (locationEnemySplit.length > 2) {
					final String secretCode = locationEnemy.split("-")[2];
					if (secretCode.equalsIgnoreCase("recuperación") ) {
						this.gameableUseCase.healing();
						this.drawable.out(this.gameableUseCase.getStringAvatarSquares() + "\r\nSe ha sanado\r\ncontinuara el ataque");
					}
				}
				final boolean counterattacked = this.gameableUseCase.attackAndCounterAttack(row, column);
				this.drawable.out(counterattacked ? "Se ha lanzado contraataque\r\n" : "Enemigo eliminado\r\n");
			} else if (locationEnemy != null && locationEnemy.startsWith("buscar:")) {
				this.drawable.out(this.gameableUseCase.getEnemies(locationEnemy));
			}
		} while (locationEnemy != null && !locationEnemy.equalsIgnoreCase("99-99") && this.player.getLife() > 0);
		
		this.drawable.out("fin del juego");
	}

	@Override
	public void getAchievements() {
	}

	@Override
	public int getStatistics(final int level) {
		return 0;
	}

}

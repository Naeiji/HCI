package alien.entities;

import alien.Alien;

/**
 * An entity which represents one of our space invader aliens.
 *
 * @author Kevin Glass
 * @author Davide Pastore
 */
public class AlienEntity extends Entity {
    /**
     * The speed at which the alien moves horizontally
     */
    private double moveSpeed = 75;
    /**
     * The game in which the entity exists
     */
    private Alien game;

    /**
     * Create a new alien entity
     *
     * @param game The game in which this entity is being created
     * @param ref  The sprite which should be displayed for this alien
     * @param x    The intial x location of this alien
     * @param y    The intial y location of this alient
     */
    public AlienEntity(Alien game, String ref, int x, int y) {
        super(ref, x, y);

        this.game = game;
        dx = -moveSpeed;
    }

    /**
     * Create a new alien entity
     *
     * @param game The game in which this entity is being created
     * @param ref  The reference to the images to be displayed for this entity
     * @param x    The initial x location of this entity
     * @param y    The initial y location of this entity
     */
    public AlienEntity(Alien game, String[] ref, int x, int y) {
        super(ref, x, y);

        this.game = game;
        dx = -moveSpeed;
    }

    /**
     * Request that this alien moved based on time elapsed
     *
     * @param delta The time that has elapsed since last move
     */
    public void move(long delta) {
        // if we have reached the left hand side of the screen and
        // are moving left then request a logic update
        if ((dx < 0) && (x < 10)) {
            game.updateLogic();
        }
        // and vice vesa, if we have reached the right hand side of
        // the screen and are moving right, request a logic update
        if ((dx > 0) && (x > 750)) {
            game.updateLogic();
        }

        // proceed with normal move
        super.move(delta);
    }

    /**
     * Update the game logic related to aliens
     */
    public void doLogic() {
        // swap over horizontal movement and move down the
        // screen a bit
        dx = -dx;
        y += 10;

        // if we've reached the bottom of the screen then the player
        // dies
        if (y > 570) {
            game.notifyDeath();
        }
    }

    /**
     * Notification that this alien has collided with another entity
     *
     * @param other The other entity
     */
    public void collidedWith(Entity other) {
        // collisions with aliens are handled elsewhere
    }
}
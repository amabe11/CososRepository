package mvvm.viewmodel;

import mvvm.model.common.FictionalCharacter;

/**
 * Convierte un personaje del modelo en datos listos para la vista.
 *
 * <p>Es la única parte del ViewModel que conoce los campos propios de cada API.
 * Gracias a ella, {@link CharacterBrowserViewModel} y toda la View son las mismas
 * para Rick and Morty y para Attack on Titan.</p>
 */
public interface CharacterFormatter<T extends FictionalCharacter> {

    CharacterItem toItem(T character);

    CharacterDetail toDetail(T character);
}

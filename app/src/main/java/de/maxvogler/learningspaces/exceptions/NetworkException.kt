package de.maxvogler.learningspaces.exceptions

public class NetworkException : Exception {

    public constructor(throwable: Throwable) : super(throwable)

    public constructor()

    public constructor(detailMessage: String) : super(detailMessage)

    public constructor(detailMessage: String, throwable: Throwable) : super(detailMessage, throwable)

}

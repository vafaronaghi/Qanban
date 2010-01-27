/*
 * Copyright 2009 Qbranch AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.qbranch.qanban


class CardEventCreate extends CardEvent {

  static constraints = {
    assigneeDomainId ( nullable : true )
    title( blank: false, length: 1..50 )
    description(blank: true, nullable: true)
    phaseDomainId( nullable: false, blank: false )
    caseNumber( blank: false )
  }

  static mapping = {
    columns {
      description type:'text'
    }
  }

  static transients = ['card','phase','board','items','assignee']

  Card card

  String title
  String description
  String caseNumber
  String phaseDomainId
  String assigneeDomainId

  //TODO: Change to checksum connections?

  public List getItems() {
    return [dateCreated, eventCreator, getCard().title]
  }

  public Phase getPhase() {
    def phase

    if( phaseDomainId ){
      phase = Phase.findByDomainId(phaseDomainId)
    }
    return phase
  }

  public Card getCard(){
    if( !card && domainId ){
      card = Card.findByDomainId(domainId)
      if(!card) {
        card = CardEventDelete.findByDomainId(domainId).card
      }
    }
    return card
  }

  public User getAssignee(){
    def assignee
    if ( assigneeDomainId )
    assignee = User.findByDomainId(assigneeDomainId)
    return assignee
  }

  //TODO: Cleanup, check lazy settings.
  public Board getBoard(){
    Phase.findByDomainId(phaseDomainId).board
  }

  def beforeInsert = {
    generateDomainId(title, caseNumber)
    userDomainId = eventCreator.domainId
  }

  def process(){
    def phase = getPhase()
    def assignee = getAssignee()
    card = new Card()
    card.domainId = domainId
    card.title = title
    card.phase = phase
    card.description = description
    card.caseNumber = caseNumber
    card.assignee = assignee
    phase.addToCards(card)
    card.save()
  }


  String toString() {
    return "$dateCreated: $eventCreator created the card"
  }
}
